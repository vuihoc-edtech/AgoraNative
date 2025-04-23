package io.agora.flat.data.repository

import io.agora.flat.data.Result
import io.agora.flat.data.ServiceFetcher
import io.agora.flat.data.model.*
import io.agora.flat.data.toResult
import io.agora.flat.http.api.CloudRecordService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

class CloudRecordRepository(
    private val cloudRecordService: CloudRecordService = ServiceFetcher.getInstance().fetchCloudRecordService(),
    private val serviceFetcher: ServiceFetcher = ServiceFetcher.getInstance(),
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

    suspend fun getRecordInfo(roomUUID: String): Result<RecordInfo> {
        return withContext(Dispatchers.IO) {
            fetchService(roomUUID).getRecordInfo(PureRoomReq(roomUUID)).toResult()
        }
    }

    private fun fetchService(roomUUID: String): CloudRecordService {
        return serviceFetcher.fetchCloudRecordService()
    }
}