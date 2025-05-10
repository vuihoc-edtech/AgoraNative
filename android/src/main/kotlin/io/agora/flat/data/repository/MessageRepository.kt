package io.agora.flat.data.repository

import io.agora.flat.common.FlatRtmException
import io.agora.flat.data.AppEnv
import io.agora.flat.data.Failure
import io.agora.flat.data.Result
import io.agora.flat.data.ServiceFetcher
import io.agora.flat.data.Success
import io.agora.flat.data.bodyOrThrow
import io.agora.flat.data.executeOnce
import io.agora.flat.data.executeWithRetry
import io.agora.flat.data.model.MessageQueryFilter
import io.agora.flat.data.model.MessageQueryHistoryReq
import io.agora.flat.data.model.RtmQueryMessage
import io.agora.flat.data.toResult
import io.agora.flat.http.api.MessageService
import io.agora.flat.http.api.MiscService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.TimeZone

class MessageRepository(
    private val messageService: MessageService = ServiceFetcher.getInstance().fetchMessageService(),
    private val miscService: MiscService = ServiceFetcher.getInstance().fetchMiscService(),
    private val userRepository: UserRepository = UserRepository.getInstance(),
    private val appEnv: AppEnv = AppEnv.getInstance(),
) {
    private var rtmToken: String? = null

    companion object {
        @Volatile
        private var INSTANCE: MessageRepository? = null

        fun getInstance(): MessageRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = MessageRepository()
                INSTANCE = instance
                instance
            }
        }
    }

    private val dateFormat: ThreadLocal<SimpleDateFormat> = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        }
    }

    suspend fun queryHistoryHandle(
        channel: String,
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int,
        order: String,
    ): Result<String> {
        val start: String = dateFormat.get().format(startTime)
        val end: String = dateFormat.get().format(endTime)
        return withContext(Dispatchers.IO) {
            try {
                if (rtmToken == null) {
                    val tokenResult = miscService.generateRtmToken().executeWithRetry().toResult()
                    if (tokenResult is Success) {
                        rtmToken = tokenResult.data.token
                    }
                }

                val result = messageService.queryHistory(
                    appEnv.agoraAppId,
                    MessageQueryHistoryReq(
                        filter = MessageQueryFilter(destination = channel, start_time = start, end_time = end),
                        limit = limit,
                        offset = offset,
                        order = order,
                    ),
                    userRepository.getUserInfo()!!.uuid,
                    rtmToken!!
                ).executeOnce()

                val location = result.bodyOrThrow().location
                if (location.isNotEmpty()) {
                    val handle = location.replace(Regex("^.*/query/"), "")
                    Success(data = handle)
                } else {
                    Failure(FlatRtmException("query history handle error"))
                }
            } catch (e: Exception) {
                Failure(e)
            }
        }
    }

    suspend fun getMessageList(handle: String): Result<List<RtmQueryMessage>> {
        return withContext(Dispatchers.IO) {
            try {
                val result = messageService.getMessageList(
                    appEnv.agoraAppId,
                    handle,
                    userRepository.getUserInfo()!!.uuid,
                    rtmToken!!
                ).executeOnce()

                val code = result.bodyOrThrow().code
                if (code == "ok") {
                    return@withContext Success(data = result.bodyOrThrow().messages)
                } else {
                    Failure(FlatRtmException("get messages error"))
                }
            } catch (e: Exception) {
                Failure(e)
            }
        }
    }

    suspend fun getMessageCount(channel: String, startTime: Long, endTime: Long): Result<Int> {
        val start: String = dateFormat.get().format(startTime)
        val end: String = dateFormat.get().format(endTime)

        return withContext(Dispatchers.IO) {
            try {
                if (rtmToken == null) {
                    val tokenResult = miscService.generateRtmToken().executeWithRetry().toResult()
                    if (tokenResult is Success) {
                        rtmToken = tokenResult.data.token
                    }
                }

                val result = messageService.getMessageCount(
                    appEnv.agoraAppId,
                    source = null,
                    destination = channel,
                    startTime = start,
                    endTime = end,
                    userRepository.getUserInfo()!!.uuid,
                    rtmToken!!
                ).executeOnce()

                val code = result.bodyOrThrow().code
                if (code == "ok") {
                    return@withContext Success(data = result.bodyOrThrow().count)
                } else {
                    Failure(FlatRtmException("get messages count error"))
                }
            } catch (e: Exception) {
                Failure(e)
            }
        }
    }
}