package io.vuihoc.agora_native.ui.activity.play

import android.util.Log
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import dagger.hilt.android.lifecycle.HiltViewModel
import io.vuihoc.agora_native.Constants
import io.vuihoc.agora_native.common.FlatException
//import io.vuihoc.agora_native.common.android.AndroidClipboardController
//import io.vuihoc.agora_native.common.android.ClipboardController
import io.vuihoc.agora_native.common.board.AgoraBoardRoom
import io.vuihoc.agora_native.common.board.DeviceState
import io.vuihoc.agora_native.common.rtc.AgoraRtc
import io.vuihoc.agora_native.common.rtc.RtcJoinOptions
import io.vuihoc.agora_native.common.rtm.ChatMessage
import io.vuihoc.agora_native.common.rtm.ClassRtmEvent
import io.vuihoc.agora_native.common.rtm.EnterRoomEvent
import io.vuihoc.agora_native.common.rtm.EventUserInfo
import io.vuihoc.agora_native.common.rtm.ExpirationWarningEvent
import io.vuihoc.agora_native.common.rtm.Message
import io.vuihoc.agora_native.common.rtm.MessageFactory
import io.vuihoc.agora_native.common.rtm.NotifyDeviceOffEvent
import io.vuihoc.agora_native.common.rtm.OnMemberJoined
import io.vuihoc.agora_native.common.rtm.OnMemberLeft
import io.vuihoc.agora_native.common.rtm.OnRemoteLogin
import io.vuihoc.agora_native.common.rtm.RaiseHandEvent
import io.vuihoc.agora_native.common.rtm.RequestDeviceEvent
import io.vuihoc.agora_native.common.rtm.RequestDeviceResponseEvent
import io.vuihoc.agora_native.common.rtm.RewardEvent
import io.vuihoc.agora_native.common.rtm.RoomBanEvent
import io.vuihoc.agora_native.common.rtm.RoomStateEvent
import io.vuihoc.agora_native.data.AppEnv
import io.vuihoc.agora_native.data.AppKVCenter
import io.vuihoc.agora_native.data.model.InviteInfo
import io.vuihoc.agora_native.data.model.RoomInfo
import io.vuihoc.agora_native.data.model.RoomPlayInfo
import io.vuihoc.agora_native.data.model.RoomStatus
import io.vuihoc.agora_native.data.model.RoomType
import io.vuihoc.agora_native.data.model.RoomUser
import io.vuihoc.agora_native.data.model.RtcShareScreen
import io.vuihoc.agora_native.data.repository.RoomRepository
import io.vuihoc.agora_native.data.repository.UserRepository
import io.vuihoc.agora_native.interfaces.RtcApi
import io.vuihoc.agora_native.interfaces.RtmApi
import io.vuihoc.agora_native.interfaces.SyncedClassState
import io.vuihoc.agora_native.event.ClassroomEvent
import io.vuihoc.agora_native.event.Event
import io.vuihoc.agora_native.event.EventBus
import io.vuihoc.agora_native.event.ExpirationEvent
import io.vuihoc.agora_native.event.MessagesAppended
import io.vuihoc.agora_native.event.NoOptPermission
import io.vuihoc.agora_native.event.NotifyDeviceOffReceived
import io.vuihoc.agora_native.event.RemoteLoginEvent
import io.vuihoc.agora_native.event.RequestDeviceReceived
import io.vuihoc.agora_native.event.RequestDeviceResponseReceived
import io.vuihoc.agora_native.event.RequestDeviceSent
import io.vuihoc.agora_native.event.RequestMuteAllSent
import io.vuihoc.agora_native.event.RewardReceived
//import io.vuihoc.agora_native.ui.manager.RecordManager
import io.vuihoc.agora_native.ui.manager.RoomErrorManager
import io.vuihoc.agora_native.ui.manager.UserManager
import io.vuihoc.agora_native.ui.viewmodel.ChatMessageManager
import io.vuihoc.agora_native.common.rtc.RtcVideoController
import io.vuihoc.agora_native.common.rtm.AgoraRtm
import io.vuihoc.agora_native.util.toInviteCodeDisplay
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class ClassRoomViewModel(
    savedStateHandle: SavedStateHandle,
    private val userManager: UserManager,
//    private val recordManager: RecordManager,
    private val messageManager: ChatMessageManager,
    private val roomErrorManager: RoomErrorManager,
    private val rtcVideoController: RtcVideoController,
    private val boardRoom: AgoraBoardRoom,
    private val syncedClassState: SyncedClassState
) : ViewModel() {

    //Init
    private val roomRepository: RoomRepository = RoomRepository.getInstance()
    private val userRepository: UserRepository = UserRepository.getInstance()
    private val rtmApi: RtmApi = AgoraRtm.getInstance()
    private val rtcApi: RtcApi = AgoraRtc.getInstance()
    private val eventbus: EventBus = EventBus.getInstance()
//    private val clipboard: ClipboardController = AndroidClipboardController.getInstance()
    private val appEnv: AppEnv = AppEnv.getInstance()
    private val appKVCenter: AppKVCenter = AppKVCenter.getInstance()
    //end init


    private var _state = MutableStateFlow<ClassRoomState?>(null)
    val state = _state.asStateFlow()

    private fun takeInitState() = state.filterNotNull().take(1)

    private var _videoUsers = MutableStateFlow<List<RoomUser>>(emptyList())
    val videoUsers = _videoUsers.asStateFlow()
    val videoUsersMap = videoUsers.map { it.associateBy { user -> user.userUUID } }

    // cloud record state
//    val recordState get() = recordManager.observeRecordState()

    val noOptPermission get() = eventbus.events.filterIsInstance<NoOptPermission>()

    val classroomEvent get() = eventbus.events.filterIsInstance<ClassroomEvent>()

    val rtcEvent get() = rtcApi.observeRtcEvent()

    val teacher = userManager.observeUsers().map { it.firstOrNull { user -> user.isOwner } }
    val students = userManager.observeUsers().map {
        it.filter { user -> !user.isOwner && (user.isJoined || user.isOnStage) }
            .map { user -> user.copy(isRaiseHand = !user.isOnStage && user.isRaiseHand) }
    }

    private var _videoAreaShown = MutableStateFlow(true)
    val videoAreaShown = _videoAreaShown.asStateFlow()

    val messageCount = messageManager.messages.map { it.size }.distinctUntilChanged()

    private val roomUUID: String = checkNotNull(savedStateHandle[Constants.IntentKey.ROOM_UUID])
    private val periodicUUID: String? = savedStateHandle[Constants.IntentKey.PERIODIC_UUID]
    private val playInfo: RoomPlayInfo? = savedStateHandle[Constants.IntentKey.ROOM_PLAY_INFO]
    private val quickStart: Boolean = savedStateHandle[Constants.IntentKey.ROOM_QUICK_START] ?: false
    private val currentUserUUID = userRepository.getUserUUID()
    private val currentUserName = userRepository.getUsername()
    private val preferDeviceState = appKVCenter.getDeviceStatePreference()
    private var syncedStoreReady = false
    private var onStageLimit = RtcApi.MAX_CAPACITY
    private var autoStageOn = false

    init {
        viewModelScope.launch {
            try {
                loadAndInitRoomState()
            } catch (e: Exception) {
               Log.d("Vuihoc_Log", "fetch class room state error $e")
                roomErrorManager.notifyError("fetch class room state error", e)
            }
        }

        /**
         * It can be determined that the status callback is made after the successful joining of the room
         */
        observeSyncedState()
    }

    fun loginThirdParty() {
        viewModelScope.launch {
            takeInitState().collect {
                joinRtm()
                joinRtc()
                joinBoard()
                syncInitState()
                observerUserState()
                if (quickStart) startClass()
            }
        }
    }

    private suspend fun loadAndInitRoomState() = coroutineScope {
        val deferredOne = async { roomRepository.getOrdinaryRoomInfo(roomUUID).getOrThrow().roomInfo }
        val deferredTwo = async {
            return@async playInfo ?: roomRepository.joinRoom(periodicUUID ?: roomUUID).getOrThrow()
        }
        initRoomState(deferredOne.await(), deferredTwo.await())
    }

    private suspend fun initRoomState(roomInfo: RoomInfo, joinRoomInfo: RoomPlayInfo) {
        val isOwner = roomInfo.ownerUUID == currentUserUUID
        Log.d("ClassRoomViewModel", "RoomInfo: $roomInfo $joinRoomInfo")
        val initState = ClassRoomState(
            userUUID = currentUserUUID,
            userName = currentUserName,
            ownerUUID = roomInfo.ownerUUID,
            ownerName = roomInfo.ownerName,
            isOwner = isOwner,

            roomUUID = roomUUID,
            title = roomInfo.title,
            roomType = roomInfo.roomType,
            inviteCode = roomInfo.inviteCode,
            isPmi = roomInfo.isPmi == true,
            beginTime = roomInfo.beginTime,
            endTime = roomInfo.endTime,
            roomStatus = roomInfo.roomStatus,
            region = roomInfo.region,

            isOnStage = isOwner,
            isRaiseHand = false,
            allowDraw = isOwner,
            videoOpen = isOwner && preferDeviceState.camera,
            audioOpen = isOwner && preferDeviceState.mic,

            boardUUID = joinRoomInfo.whiteboardRoomUUID,
            boardToken = joinRoomInfo.whiteboardRoomToken,
            rtcUID = joinRoomInfo.rtcUID,
            rtcToken = joinRoomInfo.rtcToken,
            rtcShareScreen = joinRoomInfo.rtcShareScreen,
            rtmToken = joinRoomInfo.rtmToken,
        )

        userManager.reset(
            currentUser = RoomUser(
                userUUID = currentUserUUID,
                name = currentUserName,
                avatarURL = userRepository.getUserAvatar(),
                rtcUID = initState.rtcUID,
                isOnStage = isOwner,
                isRaiseHand = initState.isRaiseHand,
                videoOpen = initState.videoOpen,
                audioOpen = initState.audioOpen,
                isOwner = isOwner,
                allowDraw = isOwner,
            ),
            ownerUUID = roomInfo.ownerUUID
        )

//        recordManager.reset(roomUUID, viewModelScope)

        onStageLimit = when (roomInfo.roomType) {
            RoomType.OneToOne -> 2
            RoomType.SmallClass -> RtcApi.MAX_CAPACITY
            RoomType.BigClass -> 2
        }

        _state.value = initState

        Log.d("Vuihoc_Log","[ROOM] init room state: $initState")
    }

    private suspend fun joinBoard() {
        Log.d("Vuihoc_Log","[BOARD] start joining board room")
        Log.d("ClassRoomViewModel", "==============[BOARD] start joining board room")
        state.value?.let {
            boardRoom.join(it.boardUUID, it.boardToken, it.region, it.isOwner)
        }
    }

    private suspend fun joinRtm() {
        Log.d("Vuihoc_Log","[RTM] start join channel")
        Log.d("ClassRoomViewModel", "[RTM] start join channel")
        state.value?.run {
            try {
                rtmApi.login(rtmToken = rtmToken, channelId = roomUUID, userUUID = userUUID)
                observerRtmEvent()
                val filterIds = rtmApi.getMembers()
                    .map { it.userId }
                    .filter { it != userUUID }
                userManager.initUsers(filterIds)
                sendEnterRoomEvent()
            } catch (e: FlatException) {
               Log.d("Vuihoc_Log", "rtm join exception $e")
                roomErrorManager.notifyError("rtm join exception", e)
            }
        }
    }

    private fun joinRtc() {
        //LogjoinRtc
         Log.d("ClassViewModel","[RTC] start join rtc")
        state.value?.run {
            val rtcJoinOptions = RtcJoinOptions(
                token = rtcToken,
                channel = roomUUID,
                uid = rtcUID,
                audioOpen = audioOpen,
                videoOpen = videoOpen
            )
            rtcVideoController.setupUid(uid = rtcUID, ssUid = rtcShareScreen.uid)
            rtcApi.joinChannel(rtcJoinOptions).also { result ->
                when (result) {
                    0 -> Log.d( "ClassRoomViewModel","\"[RTC] join rtc successful\"")
                    else -> Log.d("ClassRoomViewModel", "\"[RTC] join rtc successful\"")
                }
            }
        }
    }

    private fun observeSyncedState() {
        viewModelScope.launch {
            syncedClassState.observeDeviceState().collect {
                userManager.updateDeviceState(it)
            }
        }

//        viewModelScope.launch {
//            syncedClassState.observeClassroomState().collect {
//                userManager.updateRaiseHandStatus(it.raiseHandUsers)
//                updateBanState(it.ban)
//            }
//        }

        viewModelScope.launch {
            syncedClassState.observeOnStage().collect {
                userManager.updateOnStage(it)
                syncedStoreReady = true
            }
        }

        viewModelScope.launch {
            syncedClassState.observeWhiteboard().collect {
                userManager.updateAllowDraw(it)
            }
        }
    }

    private fun updateBanState(ban: Boolean) {
        _state.value?.run {
            _state.value = this.copy(ban = ban)
        }
    }

    private fun observerRtmEvent() {
        viewModelScope.launch {
            rtmApi.observeRtmEvent().collect {
                handleRtmEvent(it)
            }
        }
    }

    private suspend fun handleRtmEvent(event: ClassRtmEvent) {
        Log.d("Vuihoc_Log","[RTM] handle event $event")

        when (event) {
            is RaiseHandEvent -> {
                val state = state.value ?: return
                if (state.isOwner && syncedStoreReady) {
                    syncedClassState.updateRaiseHand(userId = event.sender!!, event.raiseHand)
                }
            }

            is RoomStateEvent -> {
                state.value?.run {
                    _state.value = this.copy(roomStatus = event.status)
                }
            }

            is RequestDeviceEvent -> {
                eventbus.produceEvent(RequestDeviceReceived(mic = event.mic, camera = event.camera))
            }

            is RequestDeviceResponseEvent -> {
                val uuid = event.sender ?: return
                val name = userManager.findFirstUser(uuid)?.name ?: uuid
                eventbus.produceEvent(
                    RequestDeviceResponseReceived(
                        username = name,
                        mic = event.mic,
                        camera = event.camera
                    )
                )
            }

            is NotifyDeviceOffEvent -> {
                eventbus.produceEvent(NotifyDeviceOffReceived(mic = event.mic, camera = event.camera))
            }

            is RewardEvent -> {
                if (event.sender != state.value?.ownerUUID) return
                eventbus.produceEvent(RewardReceived(event.userUUID))
            }

            is EnterRoomEvent -> {
                if (event.sender == event.userUUID) {
                    userManager.cacheUser(
                        userUUID = event.userUUID,
                        roomUser = RoomUser(
                            userUUID = event.userUUID,
                            name = event.userInfo.name,
                            avatarURL = event.userInfo.avatarURL,
                            rtcUID = event.userInfo.rtcUID,
                        )
                    )
                }
            }

            is OnMemberJoined -> {
                viewModelScope.launch {
                    // workaround: wait for EnterRoomEvent for user info
                    delay(500)
                    userManager.addUser(event.userId)
                }
            }

            is OnMemberLeft -> {
                userManager.removeUser(event.userId)
            }

            is RoomBanEvent -> {
                appendMessage(MessageFactory.createNotice(ban = event.status))
            }

            is ChatMessage -> {
                appendMessage(MessageFactory.createText(sender = event.sender, event.message))
            }

            OnRemoteLogin -> {
                eventbus.produceEvent(RemoteLoginEvent)
            }

            is ExpirationWarningEvent -> {
                eventbus.produceEvent(
                    ExpirationEvent(
                        roomLevel = event.roomLevel,
                        expireAt = event.expireAt,
                        leftMinutes = event.leftMinutes,
                    )
                )
            }

            else -> {
                Log.d("Vuihoc_Log","[RTM] event not handled: $event")
            }
        }
    }

    private fun syncInitState() {
        viewModelScope.launch {
            syncedClassState.observeSyncedReady().collect {
                val state = _state.value ?: return@collect
                if (state.isOnStage) {
                    syncedClassState.updateDeviceState(
                        userId = state.userUUID,
                        camera = state.videoOpen,
                        mic = state.audioOpen
                    )
                }
            }
        }
    }

    private fun observerUserState() {
        viewModelScope.launch {
            userManager.observeSelf().filterNotNull().collect {
                Log.d("Vuihoc_Log","[USERS] current user state changed $it, state: ${_state.value}")

                val state = _state.value ?: return@collect
                try {
                    boardRoom.setWritable(it.isOnStage || it.allowDraw)
                    boardRoom.setAllowDraw(it.allowDraw)

                    if (it.isOnStage && !state.isOnStage) {
                        syncedClassState.updateDeviceState(
                            userId = it.userUUID,
                            camera = preferDeviceState.camera,
                            mic = preferDeviceState.mic
                        )
                    }

                    _state.value = state.copy(
                        isOnStage = it.isOnStage,
                        isRaiseHand = it.isRaiseHand,
                        allowDraw = it.allowDraw,
                        videoOpen = it.videoOpen,
                        audioOpen = it.audioOpen,
                    )
                } catch (e: Exception) {
                   Log.d("Vuihoc_Log", "[USERS] self change error $e")
                }
            }
        }

        viewModelScope.launch {
            userManager.observeUsers().collect {
                Log.d("Vuihoc_Log","[USERS] users changed $it")
                val users = it.filter { user -> user.isOwner || user.isOnStage }.toMutableList()
                val devicesMap = getUpdateDevices(videoUsers.value, users)
                devicesMap.forEach { (rtcUID, state) ->
                    updateRtcStream(rtcUID, audioOpen = state.mic, videoOpen = state.camera)
                }
                _videoUsers.value = users

            }
        }

        viewModelScope.launch {
            if (userManager.isOwner() && state.value!!.roomType == RoomType.OneToOne) {
                combine(
                    syncedClassState.observeSyncedReady(),
                    userManager.observeUsers()
                ) { ready, users ->
                    if (ready) users else null
                }.filterNotNull()
                    .onCompletion {
                        Log.d("Vuihoc_Log","[USERS] one to one observeUsers done")
                    }
                    .collect {
                        val users = it.filter { user -> user.isJoined && !user.isOwner }
                        if (users.isEmpty()) return@collect
                        val count = users.count { user -> user.isOnStage }
                        if (count == 0) {
                            it.filter { user -> !user.isOnStage }.randomOrNull()?.run {
                                syncedClassState.updateOnStage(userUUID, true)
                            }
                        }
                        cancel()
                    }
            }
        }

        // small class auto stage
        viewModelScope.launch {
            if (!autoStageOn && !userManager.isOwner() && state.value!!.roomType == RoomType.SmallClass) {
                syncedClassState.observeOnStage()
                    .onCompletion {
                        Log.d("Vuihoc_Log","[USERS] small class observeOnStage done")
                    }
                    .collect { onStageUsers ->
                        if (onStageUsers.keys.size < onStageLimit && onStageUsers[currentUserUUID] != true) {
                            boardRoom.setWritable(true)
                            syncedClassState.updateOnStage(currentUserUUID, true)
                        }
                        autoStageOn = true
                        cancel()
                    }
            }
        }
    }

    private fun getUpdateDevices(
        newUsers: List<RoomUser>,
        oldUsers: List<RoomUser>
    ): MutableMap<Int, DeviceState> {
        val oldUsersMap = newUsers.associateBy { user -> user.userUUID }
        val newUsersMap = oldUsers.associateBy { user -> user.userUUID }

        val devicesMap = mutableMapOf<Int, DeviceState>()
        newUsersMap.forEach { (_, user) ->
            if (user.isJoined) {
                devicesMap[user.rtcUID] = DeviceState(
                    camera = user.isOnStage && user.videoOpen,
                    mic = user.isOnStage && user.audioOpen
                )
            }
        }
        oldUsersMap.forEach { (uuid, user) ->
            if (!newUsersMap.contains(uuid) && user.isJoined) {
                devicesMap[user.rtcUID] = DeviceState(camera = false, mic = false)
            }
        }
        return devicesMap
    }

    private fun updateRtcStream(rtcUID: Int, audioOpen: Boolean, videoOpen: Boolean) {
        if (rtcUID == userManager.currentUser?.rtcUID) {
            rtcApi.updateLocalStream(audio = audioOpen, video = videoOpen)
        } else {
            rtcApi.updateRemoteStream(rtcUid = rtcUID, audio = audioOpen, video = videoOpen)
        }
    }

    fun canControlDevice(uuid: String): Boolean {
        val state = _state.value ?: return false
        return state.isOwner || (state.isOnStage && uuid == state.userUUID)
    }

    fun enableVideo(enableVideo: Boolean, uuid: String = currentUserUUID) {
        viewModelScope.launch {
            val state = _state.value ?: return@launch
            userManager.findFirstUser(uuid)?.run {
                if (userManager.isUserSelf(uuid)) {
                    updateDeviceState(uuid, enableVideo = enableVideo, enableAudio = audioOpen)
                } else {
                    if (state.isOwner) {
                        if (enableVideo) {
                            rtmApi.sendPeerCommand(RequestDeviceEvent(roomUUID = roomUUID, camera = true), uuid)
                            eventbus.produceEvent(RequestDeviceSent(camera = true))
                        } else {
                            updateDeviceState(uuid, enableVideo = false, enableAudio = audioOpen)
                        }
                    }
                }
            }
        }
    }

    fun enableAudio(enableAudio: Boolean, uuid: String = currentUserUUID) {
        viewModelScope.launch {
            val state = _state.value ?: return@launch
            userManager.findFirstUser(uuid)?.run {
                if (userManager.isUserSelf(uuid)) {
                    updateDeviceState(uuid, enableVideo = videoOpen, enableAudio = enableAudio)
                } else if (state.isOwner) {
                    if (enableAudio) {
                        rtmApi.sendPeerCommand(RequestDeviceEvent(roomUUID = roomUUID, mic = true), uuid)
                        eventbus.produceEvent(RequestDeviceSent(mic = true))
                    } else {
                        updateDeviceState(uuid, enableVideo = videoOpen, enableAudio = false)
                    }
                }
            }
        }
    }

    private fun updateDeviceState(userUUID: String, enableVideo: Boolean, enableAudio: Boolean) {
        syncedClassState.updateDeviceState(userUUID, camera = enableVideo, mic = enableAudio)
    }

    fun sendGlobalEvent(event: Event) {
        viewModelScope.launch {
            eventbus.produceEvent(event)
        }
    }

    fun setVideoAreaShown(shown: Boolean) {
        _videoAreaShown.value = shown
    }

    private fun appendMessage(message: Message) {
        viewModelScope.launch {
            eventbus.produceEvent(MessagesAppended(listOf(message)))
        }
    }

    fun raiseHand() {
//        viewModelScope.launch {
//            userManager.currentUser?.run {
//                if (isOnStage || !userManager.isOwnerOnStage()) return@run
//                val raiseHand = !isRaiseHand
//                rtmApi.sendPeerCommand(
//                    RaiseHandEvent(roomUUID = roomUUID, raiseHand = raiseHand),
//                    userManager.ownerUUID
//                )
//                userManager.updateRaiseHandStatus(userUUID, raiseHand)
//            }
//        }
    }

    private fun startClass() {
        viewModelScope.launch {
            val state = _state.value ?: return@launch
            if (roomRepository.startRoomClass(roomUUID).isSuccess) {
                rtmApi.sendChannelCommand(RoomStateEvent(roomUUID = roomUUID, status = RoomStatus.Started))
                _state.value = state.copy(roomStatus = RoomStatus.Started)
            } else {
               Log.d("Vuihoc_Log","start class error")
            }
        }
    }

    suspend fun pauseClass(): Boolean {
        val result = roomRepository.pauseRoomClass(roomUUID)
        if (result.isSuccess) {
            rtmApi.sendChannelCommand(RoomStateEvent(roomUUID = roomUUID, status = RoomStatus.Paused))
        }
        return result.isSuccess
    }

    suspend fun stopClass(): Boolean {
        val result = roomRepository.stopRoomClass(roomUUID)
        if (result.isSuccess) {
            rtmApi.sendChannelCommand(RoomStateEvent(roomUUID = roomUUID, status = RoomStatus.Stopped))
//            recordManager.stopRecord(true)
        }
        return result.isSuccess
    }

    suspend fun startRecord() {
//        recordManager.startRecord()
    }

    suspend fun stopRecord() {
//        recordManager.stopRecord()
    }

    fun setClipboard(text: String) {
//        clipboard.putText(text)
    }

    fun updateOnStage(uuid: String, onstage: Boolean) {
        viewModelScope.launch {
            if (onstage) {
                acceptRaiseHand(uuid)
            } else {
                closeSpeak(uuid)
            }
        }
    }

    fun updateAllowDraw(uuid: String, allowDraw: Boolean) {
        viewModelScope.launch {
            val state = _state.value ?: return@launch
            if (state.isOwner) {
                syncedClassState.updateWhiteboard(uuid, allowDraw)
            }
            if (userManager.isUserSelf(uuid) && !allowDraw) {
                syncedClassState.updateWhiteboard(uuid, false)
            }
        }
    }

    private fun closeSpeak(uuid: String) {
        viewModelScope.launch {
            val state = _state.value ?: return@launch
            if (state.isOwner || userManager.isUserSelf(uuid)) {
                syncedClassState.deleteDeviceState(uuid)
                syncedClassState.updateOnStage(uuid, false)
            }
        }
    }

    fun acceptRaiseHand(uuid: String) {
//        viewModelScope.launch {
//            if (!userManager.isOwner()) return@launch
//            if (userManager.getOnStageCount() < onStageLimit) {
//                syncedClassState.updateOnStage(uuid, true)
//                syncedClassState.updateRaiseHand(uuid, false)
//            } else {
//                // notify
//            }
//        }
    }

    fun cancelRaiseHand(uuid: String) {
//        viewModelScope.launch {
//            if (!userManager.isOwner()) return@launch
//            syncedClassState.updateRaiseHand(uuid, false)
//        }
    }

    fun getInviteInfo(): InviteInfo? {
        val state = state.value ?: return null
//        val linkCode = if (state.isPmi) state.inviteCode else state.roomUUID

        return InviteInfo(
            username = currentUserName,
            roomTitle = state.title,
            link = "",
            roomUuid = state.inviteCode.toInviteCodeDisplay(),
            beginTime = state.beginTime,
            endTime = state.endTime,
            isPmi = state.isPmi,
        )
    }

    fun isOwner(): Boolean {
        return userManager.isOwner()
    }

    fun isSelf(userId: String): Boolean {
        return userManager.isUserSelf(userId)
    }

    fun isOnStageAllowable(): Boolean {
        return userManager.getOnStageCount() < onStageLimit
    }

    fun canShowCallOut(userId: String): Boolean {
        return userManager.isOwner() || userManager.isUserSelf(userId)
    }

    fun canDragUser(): Boolean {
        return userManager.isOwner()
    }

    fun stageOffAll() {
        viewModelScope.launch {
            val state = _state.value ?: return@launch
            if (state.isOwner) {
                syncedClassState.stageOffAll()
            }
        }
    }

    fun muteAllMic() {
        viewModelScope.launch {
            val state = _state.value ?: return@launch
            if (state.isOwner) {
                val userIds = _videoUsers.value.filter { !it.isOwner }.map { user -> user.userUUID }
                syncedClassState.muteDevicesMic(userIds)
                eventbus.produceEvent(RequestMuteAllSent)
            }
        }
    }

    fun refuseCamera() {
        viewModelScope.launch {
            rtmApi.sendPeerCommand(
                RequestDeviceResponseEvent(roomUUID = roomUUID, camera = false),
                userManager.ownerUUID
            )
        }
    }

    fun agreeCamera() {
        viewModelScope.launch {
            if (syncedStoreReady) {
                enableVideo(true)
                rtmApi.sendPeerCommand(
                    RequestDeviceResponseEvent(roomUUID = roomUUID, camera = true),
                    userManager.ownerUUID
                )
            } else {
                refuseCamera()
            }
        }
    }

    fun refuseMic() {
        viewModelScope.launch {
            rtmApi.sendPeerCommand(
                RequestDeviceResponseEvent(roomUUID = roomUUID, mic = false),
                userManager.ownerUUID
            )
        }
    }

    fun agreeMic() {
        viewModelScope.launch {
            if (syncedStoreReady) {
                enableAudio(true)
                rtmApi.sendPeerCommand(
                    RequestDeviceResponseEvent(roomUUID = roomUUID, mic = true),
                    userManager.ownerUUID
                )
            } else {
                refuseMic()
            }
        }
    }

    fun sendReward(userUUID: String) {
        viewModelScope.launch {
            rtmApi.sendChannelCommand(
                RewardEvent(roomUUID = roomUUID, userUUID = userUUID)
            )
        }
    }

    private fun sendEnterRoomEvent() {
        viewModelScope.launch {
            val state = _state.value ?: return@launch
            rtmApi.sendChannelCommand(
                EnterRoomEvent(
                    roomUUID = roomUUID,
                    userUUID = currentUserUUID,
                    userInfo = EventUserInfo(
                        rtcUID = state.rtcUID,
                        name = currentUserName,
                        avatarURL = userRepository.getUserAvatar()
                    )
                )
            )
        }
    }
}

data class ClassRoomState(
    // users
    val userUUID: String,
    val userName: String = "",
    val ownerUUID: String,
    val ownerName: String,
    val isOwner: Boolean,

    // current user state
    val isOnStage: Boolean,
    val isRaiseHand: Boolean,
    val allowDraw: Boolean,
    val videoOpen: Boolean,
    val audioOpen: Boolean,

    // current user interaction params
    val boardUUID: String,
    val boardToken: String,
    val rtcUID: Int,
    val rtcToken: String,
    val rtcShareScreen: RtcShareScreen,
    val rtmToken: String,

    // class room info
    // 房间标题
    val title: String,
    // 房间的 uuid
    val roomUUID: String,
    // 房间开始时间
    val beginTime: Long = 0L,
    // 结束时间
    val endTime: Long = 0L,
    // 房间邀请码
    val inviteCode: String,

    val isPmi: Boolean = false,
    // 房间类型
    val roomType: RoomType,
    // 房间区域
    val region: String,
    // 禁言
    val ban: Boolean = false,
    // 房间状态
    val roomStatus: RoomStatus,
)

data class ImageInfo(val width: Int, val height: Int, val orientation: Int)

class ClassRoomViewModelFactory(
    private val userManager: UserManager,
//    private val recordManager: RecordManager,
    private val messageManager: ChatMessageManager,
    private val roomErrorManager: RoomErrorManager,
    private val rtcVideoController: RtcVideoController,
    private val boardRoom: AgoraBoardRoom,
    private val syncedClassState: SyncedClassState,
) : AbstractSavedStateViewModelFactory() {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return ClassRoomViewModel(
            handle,
            userManager = userManager,
//            recordManager = recordManager,
            messageManager = messageManager,
            roomErrorManager = roomErrorManager,
            rtcVideoController = rtcVideoController,
            boardRoom = boardRoom,
            syncedClassState = syncedClassState,
        ) as T
    }
}
