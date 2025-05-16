package io.agora.flat.data.repository

import io.agora.flat.data.ServiceFetcher
import io.agora.flat.data.model.RtmCensorReq
import io.agora.flat.data.toResult
import io.agora.flat.http.api.MiscService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MiscRepository(
    private val miscService: MiscService = ServiceFetcher.getInstance().fetchMiscService(),
) {

    companion object {
        @Volatile
        private var INSTANCE: MiscRepository? = null

        fun getInstance(
        ): MiscRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = MiscRepository()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun censorRtm(text: String): Boolean {
        return withContext(Dispatchers.IO) {
            val result = miscService.censorRtm(RtmCensorReq(text)).toResult()
            return@withContext result.get()?.valid == true
        }
    }
}