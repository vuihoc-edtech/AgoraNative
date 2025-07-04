package io.vuihoc.agora_native.common.rtm

import android.content.Context
import android.util.Log
import io.vuihoc.agora_native.common.FlatException
import io.vuihoc.agora_native.common.FlatRtmException
import io.vuihoc.agora_native.data.AppEnv
import io.vuihoc.agora_native.data.AppKVCenter
import io.vuihoc.agora_native.data.Success
import io.vuihoc.agora_native.data.model.RtmQueryMessage
import io.vuihoc.agora_native.data.repository.MessageRepository
import io.vuihoc.agora_native.interfaces.PostLoginInitializer
import io.vuihoc.agora_native.interfaces.RtmApi
import io.agora.rtm.ErrorInfo
import io.agora.rtm.MessageEvent
import io.agora.rtm.PresenceEvent
import io.agora.rtm.PresenceOptions
import io.agora.rtm.PublishOptions
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmConstants.RtmChannelType
import io.agora.rtm.RtmEventListener
import io.agora.rtm.SubscribeOptions
import io.agora.rtm.UserState
import io.agora.rtm.WhoNowResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * TODO messageEvent.publisherId == "flat-server"
 */
@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class AgoraRtm(
    private val messageRepository: MessageRepository,
    private val appEnv: AppEnv = AppEnv.getInstance(),
    private val appKVCenter: AppKVCenter = AppKVCenter.getInstance(),
) : RtmApi, PostLoginInitializer, RtmEventListener {
    private lateinit var rtmClient: RtmClient
    private var currentChannel: String? = null
    private var rtmListeners = mutableListOf<RtmListener>()

    companion object {
        @Volatile
        private var INSTANCE: AgoraRtm? = null

        fun getInstance(): AgoraRtm {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AgoraRtm(
                    MessageRepository.getInstance(), // You should ensure this is a singleton too
                    AppEnv.getInstance(),
                    AppKVCenter.getInstance()
                ).also { INSTANCE = it }
            }
        }
    }

    override fun init(context: Context) {
        RtmClient.release()
        try {
            val config = RtmConfig.Builder(appEnv.agoraAppId, appKVCenter.getUserInfo()?.uuid)
                .eventListener(this)
                .build()
            rtmClient = RtmClient.create(config)
        } catch (e: Exception) {
           Log.d("Vuihoc_Log", "[RTM] agora rtm SDK init fatal error! $e")
        }
    }

    override fun onMessageEvent(messageEvent: MessageEvent) {
        val publisherId = messageEvent.publisherId
        val messageData = messageEvent.message.data
        Log.d("Vuihoc_Log","[RTM] message received from $publisherId message:$messageData")
        val parseAndNotify: (String) -> Unit = { parsedMessage ->
            rtmListeners.forEach { it.onClassEvent(ClassRtmEvent.parse(parsedMessage, publisherId)) }
        }
        if (messageEvent.channelType == RtmChannelType.USER) {
            parseAndNotify(String(messageData as ByteArray))
        } else if (currentChannel == messageEvent.channelName) {
            when (messageEvent.message.type) {
                RtmConstants.RtmMessageType.BINARY -> {
                    parseAndNotify(String(messageData as ByteArray))
                }

                RtmConstants.RtmMessageType.STRING -> {
                    rtmListeners.forEach {
                        if (publisherId == "flat-server") {
                            it.onClassEvent(ClassRtmEvent.parseSys(messageData as String, publisherId))
                        } else {
                            it.onChatMessage(ChatMessage(messageData as String, publisherId))
                        }
                    }
                }
            }
        }
    }

    override fun onPresenceEvent(event: PresenceEvent) {
        Log.d("Vuihoc_Log","[RTM] presence event $event")
        if (currentChannel != event.channelName) return

        val channelName = event.channelName
        val publisherId = event.publisherId

        fun notifyJoined(userId: String) {
            rtmListeners.forEach { it.onMemberJoined(userId, channelName) }
        }

        fun notifyLeft(userId: String) {
            rtmListeners.forEach { it.onMemberLeft(userId, channelName) }
        }

        when (event.eventType) {
            RtmConstants.RtmPresenceEventType.SNAPSHOT,
            RtmConstants.RtmPresenceEventType.REMOTE_JOIN -> notifyJoined(publisherId)

            RtmConstants.RtmPresenceEventType.INTERVAL -> {
                event.interval.leaveUserList.forEach(::notifyLeft)
                event.interval.timeoutUserList.forEach(::notifyLeft)
                event.interval.joinUserList.forEach(::notifyJoined)
            }

            RtmConstants.RtmPresenceEventType.REMOTE_LEAVE,
            RtmConstants.RtmPresenceEventType.REMOTE_TIMEOUT -> notifyLeft(publisherId)

            RtmConstants.RtmPresenceEventType.REMOTE_STATE_CHANGED -> {
                Log.d("Vuihoc_Log","[RTM] presence event remote state changed")
            }

            RtmConstants.RtmPresenceEventType.ERROR_OUT_OF_SERVICE -> {
                Log.d("Vuihoc_Log","[RTM] presence event error out of service")
            }

            RtmConstants.RtmPresenceEventType.NONE -> {
                Log.d("Vuihoc_Log","[RTM] presence event none")
            }
        }
    }

    override fun onConnectionStateChanged(
        channelName: String,
        state: RtmConstants.RtmConnectionState,
        reason: RtmConstants.RtmConnectionChangeReason
    ) {
        Log.d("Vuihoc_Log","[RTM] connection state changes to $state reason:$reason")
        if (reason == RtmConstants.RtmConnectionChangeReason.SAME_UID_LOGIN) {
            rtmListeners.forEach { it.onRemoteLogin() }
        }
    }

    override fun onTokenPrivilegeWillExpire(channelName: String) {
        Log.d("Vuihoc_Log","[RTM] token privilege will expire $channelName")
    }

    override suspend fun login(rtmToken: String, channelId: String, userUUID: String): Boolean {
        return runCatching {
            login(rtmToken)
            joinChannel(channelId)
        }.isSuccess
    }

    private suspend fun login(rtmToken: String): Boolean = suspendCoroutine { cont ->
        rtmClient.login(rtmToken, object : ResultCallback<Void?> {
            override fun onSuccess(v: Void?) = cont.resume(true)
            override fun onFailure(e: ErrorInfo) = cont.resumeWithException(e.toFlatException())
        })
    }

    private suspend fun joinChannel(channelId: String): Boolean = suspendCoroutine { cont ->
        val options = SubscribeOptions().apply {
            withMessage = true
            withPresence = true
        }

        rtmClient.subscribe(channelId, options, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                this@AgoraRtm.currentChannel = channelId
                cont.resume(true)
            }

            override fun onFailure(e: ErrorInfo) = cont.resumeWithException(e.toFlatException())
        })
    }

    override suspend fun logout(): Boolean = suspendCoroutine { cont ->
        rtmClient.logout(object : ResultCallback<Void?> {
            override fun onSuccess(v: Void?) = cont.resume(true)
            override fun onFailure(e: ErrorInfo) = cont.resume(false)
        })
    }

    override suspend fun getMembers(): List<RtmMember> {
        val channelName = currentChannel ?: return emptyList()
        val users = mutableListOf<UserState>()
        var nextPage: String? = ""

        try {
            do {
                val result = this.onePageWhoNow(channelName, nextPage)
                users.addAll(result.userStateList)
                nextPage = result.nextPage
            } while (!nextPage.isNullOrEmpty())
        } catch (e: Exception) {
           Log.d("Vuihoc_Log", "[RTM] getMembers error $e")
        }

        return users.map { RtmMember(it.userId, channelName) }
    }

    private suspend fun onePageWhoNow(
        channelName: String,
        page: String? = ""
    ): WhoNowResult = suspendCoroutine { cont ->
        val options = PresenceOptions().apply {
            includeUserId = true
            this.page = page
        }
        rtmClient.presence.whoNow(channelName, RtmChannelType.MESSAGE, options, object : ResultCallback<WhoNowResult> {
            @Override
            override fun onSuccess(result: WhoNowResult) {
                cont.resume(result)
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                cont.resumeWithException(errorInfo.toFlatException())
            }
        })
    }

    override suspend fun sendChannelMessage(msg: String): Boolean = suspendCoroutine { cont ->
        Log.d("Vuihoc_Log","[RTM] sendChannelMessage $msg")
        val options = PublishOptions().apply {
            customType = "CHANNEL_CHAT"
        }
        rtmClient.publish(currentChannel, msg, options, object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                cont.resume(true)
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                cont.resume(false)
            }
        })
    }

    override suspend fun sendChannelCommand(event: ClassRtmEvent) = suspendCoroutine { cont ->
        Log.d("Vuihoc_Log","[RTM] sendChannelCommand ${ClassRtmEvent.toText(event)}")
        val options = PublishOptions().apply {
            customType = "CHANNEL_COMMAND"
        }
        val msg = ClassRtmEvent.toText(event).toByteArray()
        rtmClient.publish(currentChannel, msg, options, object : ResultCallback<Void?> {
            override fun onSuccess(v: Void?) {
                cont.resume(true)
            }

            override fun onFailure(error: ErrorInfo) {
                cont.resume(false)
            }
        })
    }

    override suspend fun sendPeerCommand(event: ClassRtmEvent, peerId: String) = suspendCoroutine { cont ->
        Log.d("Vuihoc_Log","[RTM] sendPeerCommand ${ClassRtmEvent.toText(event)}")
        val message = ClassRtmEvent.toText(event).toByteArray()
        val options = PublishOptions().apply {
            setChannelType(RtmChannelType.USER)
            customType = "PEER_COMMAND"
        }
        rtmClient.publish(peerId, message, options, object : ResultCallback<Void?> {
            override fun onSuccess(v: Void?) {
                cont.resume(true)
            }

            override fun onFailure(error: ErrorInfo) {
                cont.resume(false)
            }
        })
    }

    override suspend fun getTextHistory(
        channelId: String,
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int,
        order: String,
    ): List<RtmQueryMessage> {
        val result = messageRepository.queryHistoryHandle(channelId, startTime, endTime, limit, offset, order)
        if (result is Success) {
            repeat(3) {
                delay(2000)
                val messageResult = messageRepository.getMessageList(handle = result.data)
                if (messageResult is Success) {
                    return messageResult.data
                }
            }
        }
        return listOf()
    }

    override suspend fun getTextHistoryCount(channelId: String, startTime: Long, endTime: Long): Int {
        repeat(3) {
            delay(2000)
            val result = messageRepository.getMessageCount(channelId, startTime, endTime)
            if (result is Success) {
                return result.data
            }
        }
        return -1
    }

    override fun observeRtmEvent(): Flow<ClassRtmEvent> = callbackFlow {
        val listener = object : RtmListener {
            override fun onClassEvent(event: ClassRtmEvent) {
                trySend(event)
            }

            override fun onChatMessage(chatMessage: ChatMessage) {
                trySend(chatMessage)
            }

            override fun onRemoteLogin() {
                trySend(OnRemoteLogin)
            }

            override fun onMemberJoined(userId: String, channelId: String) {
                trySend(OnMemberJoined(userId = userId, channelId = channelId))
            }

            override fun onMemberLeft(userId: String, channelId: String) {
                trySend(OnMemberLeft(userId = userId, channelId = channelId))
            }
        }
        rtmListeners.add(listener)
        awaitClose {
            Log.d("Vuihoc_Log","[RTM] rtm event flow closed")
            rtmListeners.remove(listener)
        }
    }

    internal fun ErrorInfo.toFlatException(): FlatException {
        return FlatRtmException(this.errorReason, RtmConstants.RtmErrorCode.getValue(this.errorCode))
    }
}