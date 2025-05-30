package io.vuihoc.agora_native.data.repository

import io.vuihoc.agora_native.data.Result
import io.vuihoc.agora_native.data.ServiceFetcher
import io.vuihoc.agora_native.data.model.AgoraRecordMode
import io.vuihoc.agora_native.data.model.AgoraRecordParams
import io.vuihoc.agora_native.data.model.AgoraRecordStartedData
import io.vuihoc.agora_native.data.model.AgoraRecordUpdateLayoutData
import io.vuihoc.agora_native.data.model.ClientRequest
import io.vuihoc.agora_native.data.model.RecordAcquireReq
import io.vuihoc.agora_native.data.model.RecordAcquireReqData
import io.vuihoc.agora_native.data.model.RecordAcquireReqDataClientRequest
import io.vuihoc.agora_native.data.model.RecordAcquireRespData
import io.vuihoc.agora_native.data.model.RecordQueryRespData
import io.vuihoc.agora_native.data.model.RecordReq
import io.vuihoc.agora_native.data.model.RecordStartReq
import io.vuihoc.agora_native.data.model.RecordStartRespData
import io.vuihoc.agora_native.data.model.RecordStopRespData
import io.vuihoc.agora_native.data.model.RecordUpdateLayoutReq
import io.vuihoc.agora_native.data.model.RecordingConfig
import io.vuihoc.agora_native.data.model.TranscodingConfig
import io.vuihoc.agora_native.data.model.UpdateLayoutClientRequest
import io.vuihoc.agora_native.data.toResult
import io.vuihoc.agora_native.http.api.CloudRecordService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloudRecordRepository(
    private val cloudRecordService: CloudRecordService = ServiceFetcher.getInstance()
        .fetchCloudRecordService(),
) {

    companion object {
        @Volatile
        private var INSTANCE: CloudRecordRepository? = null

        fun getInstance(): CloudRecordRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = CloudRecordRepository()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun acquireRecord(roomUUID: String, expiredHour: Int = 24): Result<RecordAcquireRespData> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.acquireRecord(
                RecordAcquireReq(
                    roomUUID,
                    RecordAcquireReqData(RecordAcquireReqDataClientRequest(expiredHour, 0))
                )
            ).toResult()
        }
    }

    suspend fun startRecordWithAgora(
        roomUUID: String,
        resourceId: String,
        transcodingConfig: TranscodingConfig,
        mode: AgoraRecordMode = AgoraRecordMode.Mix,
    ): Result<RecordStartRespData> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.startRecordWithAgora(
                RecordStartReq(
                    roomUUID,
                    AgoraRecordParams(resourceId, mode),
                    AgoraRecordStartedData(
                        ClientRequest(
                            RecordingConfig(
                                subscribeUidGroup = 0,
                                transcodingConfig = transcodingConfig
                            )
                        )
                    )
                )
            ).toResult()
        }
    }

    suspend fun queryRecordWithAgora(
        roomUUID: String,
        resourceId: String,
        sid: String,
        mode: AgoraRecordMode = AgoraRecordMode.Mix,
    ): Result<RecordQueryRespData> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.queryRecordWithAgora(
                RecordReq(
                    roomUUID,
                    AgoraRecordParams(resourceid = resourceId, mode = mode, sid = sid),
                )
            ).toResult()
        }
    }

    suspend fun updateRecordLayoutWithAgora(
        roomUUID: String,
        resourceId: String,
        sid: String,
        clientRequest: UpdateLayoutClientRequest,
        mode: AgoraRecordMode = AgoraRecordMode.Mix,
    ): Result<RecordQueryRespData> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.updateRecordLayoutWithAgora(
                RecordUpdateLayoutReq(
                    roomUUID,
                    AgoraRecordParams(resourceid = resourceId, mode = mode, sid = sid),
                    AgoraRecordUpdateLayoutData(clientRequest)
                )
            ).toResult()
        }
    }

    suspend fun stopRecordWithAgora(
        roomUUID: String,
        resourceId: String,
        sid: String,
        mode: AgoraRecordMode = AgoraRecordMode.Mix,
    ): Result<RecordStopRespData> {
        return withContext(Dispatchers.IO) {
            cloudRecordService.stopRecordWithAgora(
                RecordReq(
                    roomUUID,
                    AgoraRecordParams(resourceId, mode, sid),
                )
            ).toResult()
        }
    }
}