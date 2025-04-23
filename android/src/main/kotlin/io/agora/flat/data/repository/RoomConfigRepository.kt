package io.agora.flat.data.repository

import io.agora.flat.data.dao.RoomConfigDao
import io.agora.flat.data.model.RoomConfig
import io.agora.flat.http.api.MiscService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

class RoomConfigRepository(
    private val roomConfigDao: RoomConfigDao,
) {
    companion object {
        @Volatile
        private var INSTANCE: RoomConfigRepository? = null

        fun getInstance(
            roomConfigDao: RoomConfigDao,
        ): RoomConfigRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = RoomConfigRepository(roomConfigDao)
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun updateRoomConfig(roomConfig: RoomConfig) {
        return withContext(Dispatchers.IO) {
            roomConfigDao.insertOrUpdate(roomConfig)
        }
    }

    suspend fun getRoomConfig(uuid: String): RoomConfig? {
        return withContext(Dispatchers.IO) {
            roomConfigDao.getConfigById(uuid)
        }
    }
}