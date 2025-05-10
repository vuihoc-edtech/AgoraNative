package io.agora.flat.data.repository

import io.agora.flat.data.Result
import io.agora.flat.data.ServiceFetcher
import io.agora.flat.data.model.AgoraRecordMode
import io.agora.flat.data.model.AgoraRecordParams
import io.agora.flat.data.model.AgoraRecordStartedData
import io.agora.flat.data.model.AgoraRecordUpdateLayoutData
import io.agora.flat.data.model.ClientRequest
import io.agora.flat.data.model.RecordAcquireReq
import io.agora.flat.data.model.RecordAcquireReqData
import io.agora.flat.data.model.RecordAcquireReqDataClientRequest
import io.agora.flat.data.model.RecordAcquireRespData
import io.agora.flat.data.model.RecordQueryRespData
import io.agora.flat.data.model.RecordReq
import io.agora.flat.data.model.RecordStartReq
import io.agora.flat.data.model.RecordStartRespData
import io.agora.flat.data.model.RecordStopRespData
import io.agora.flat.data.model.RecordUpdateLayoutReq
import io.agora.flat.data.model.RecordingConfig
import io.agora.flat.data.model.TranscodingConfig
import io.agora.flat.data.model.UpdateLayoutClientRequest
import io.agora.flat.data.toResult
import io.agora.flat.http.api.CloudRecordService
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