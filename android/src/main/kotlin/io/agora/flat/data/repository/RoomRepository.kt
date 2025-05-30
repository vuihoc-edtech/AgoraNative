package io.agora.flat.data.repository

import io.agora.flat.common.android.I18NFetcher
import io.agora.flat.data.AppKVCenter
import io.agora.flat.data.Result
import io.agora.flat.data.ServiceFetcher
import io.agora.flat.data.manager.JoinRoomRecordManager
import io.agora.flat.data.model.JoinRoomRecord
import io.agora.flat.data.model.JoinRoomReq
import io.agora.flat.data.model.NetworkRoomUser
import io.agora.flat.data.model.PureRoomReq
import io.agora.flat.data.model.RespNoData
import io.agora.flat.data.model.RoomDetailOrdinary
import io.agora.flat.data.model.RoomDetailOrdinaryReq
import io.agora.flat.data.model.RoomPlayInfo
import io.agora.flat.data.model.RoomUsersReq
import io.agora.flat.data.toResult
import io.agora.flat.http.api.RoomService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomRepository(
    private val serviceFetcher: ServiceFetcher = ServiceFetcher.getInstance(),
    private val joinRoomRecordManager: JoinRoomRecordManager = JoinRoomRecordManager.getInstance(),
    private val appKVCenter: AppKVCenter = AppKVCenter.getInstance(),
    private val i18NFetcher: I18NFetcher = I18NFetcher.getInstance(),
) {
    companion object {
        @Volatile
        private var INSTANCE: RoomRepository? = null

        fun getInstance(): RoomRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = RoomRepository()
                INSTANCE = instance
                instance
            }
        }
    }

    private fun fetchService(uuid: String): RoomService {
        return serviceFetcher.fetchRoomService()
    }

    suspend fun getOrdinaryRoomInfo(roomUUID: String): Result<RoomDetailOrdinary> {
        return withContext(Dispatchers.IO) {
            fetchService(roomUUID).getOrdinaryRoomInfo(RoomDetailOrdinaryReq(roomUUID = roomUUID)).toResult().also {
                it.get()?.roomInfo?.run {
                    if (ownerUUID == appKVCenter.getUserInfo()?.uuid) return@run
                    // treat 32 length or longer string as long uuid
                    if (inviteCode.length >= 32) return@run
                    val title = if (isPmi == true) {
                        i18NFetcher.getString(I18NFetcher.JOIN_ROOM_RECORD_PMI_TITLE, ownerUserName)
                    } else {
                        this.title
                    }
                    joinRoomRecordManager.addRecord(JoinRoomRecord(title, inviteCode))
                }
            }
        }
    }

    suspend fun joinRoom(roomUUID: String): Result<RoomPlayInfo> {
        return withContext(Dispatchers.IO) {
            fetchService(roomUUID).joinRoom(JoinRoomReq(roomUUID)).toResult()
        }
    }

    suspend fun getRoomUsers(roomUUID: String, usersUUID: List<String>?): Result<Map<String, NetworkRoomUser>> {
        return withContext(Dispatchers.IO) {
            fetchService(roomUUID).getRoomUsers(RoomUsersReq(roomUUID, usersUUID)).toResult()
        }
    }

    suspend fun startRoomClass(roomUUID: String): Result<RespNoData> {
        return withContext(Dispatchers.IO) {
            fetchService(roomUUID).startRoomClass(PureRoomReq(roomUUID)).toResult()
        }
    }

    suspend fun pauseRoomClass(roomUUID: String): Result<RespNoData> {
        return withContext(Dispatchers.IO) {
            fetchService(roomUUID).pauseRoomClass(PureRoomReq(roomUUID)).toResult()
        }
    }

    suspend fun stopRoomClass(roomUUID: String): Result<RespNoData> {
        return withContext(Dispatchers.IO) {
            fetchService(roomUUID).stopRoomClass(PureRoomReq(roomUUID)).toResult()
        }
    }
}