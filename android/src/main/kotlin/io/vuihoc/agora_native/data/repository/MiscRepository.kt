package io.vuihoc.agora_native.data.repository

import io.vuihoc.agora_native.data.ServiceFetcher
import io.vuihoc.agora_native.data.model.RtmCensorReq
import io.vuihoc.agora_native.data.toResult
import io.vuihoc.agora_native.http.api.MiscService
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