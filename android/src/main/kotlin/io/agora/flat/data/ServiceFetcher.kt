package io.agora.flat.data

import io.agora.flat.http.api.CloudRecordService
import io.agora.flat.http.api.MessageService
import io.agora.flat.http.api.MiscService
import io.agora.flat.http.api.RoomService
import io.agora.flat.http.api.UserService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


/**
 * Fetch room service by uuid
 *
 * For joining rooms between different regions
 */
class ServiceFetcher(
    private val client: OkHttpClient = OkHttpClient(),
    private val appEnv: AppEnv = AppEnv.getInstance()
) {
    companion object {

        @Volatile
        private var INSTANCE: ServiceFetcher? = null

        fun getInstance(): ServiceFetcher {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ServiceFetcher().also { INSTANCE = it }
            }
        }
        private val regions = listOf(
            "CN",
            "SG",
        )

        private val codeMap = mapOf(
            "1" to "CN",
            "2" to "SG",
        )

        fun fetchEnv(uuid: String, currentEnv: String): String {
            var (region, envType) = currentEnv.split("_")

            // short invite code
            if (uuid.length == 11) {
                val code = uuid[0] + ""
                codeMap[code]?.let { region = it }
            }

            // long uuid
            if (uuid.length > 15) {
                val firstTwo = uuid.substring(0, 2)
                regions.find { it == firstTwo.uppercase() }?.let { region = it }
            }

            return "${region}_$envType".lowercase()
        }
    }

    private val allCache = mutableMapOf<Pair<String, String>, Any>()

    fun fetchRoomService(): RoomService {
        return getApiService<RoomService>()
    }

    fun fetchUserService(): UserService {
        return getApiService<UserService>()
    }

    fun fetchCloudRecordService(): CloudRecordService {
        return getApiService<CloudRecordService>()
    }

    fun fetchMessageService(): MessageService {
        return getApiService<MessageService>()
    }

    fun fetchMiscService(): MiscService {
        return getApiService<MiscService>()
    }

    private inline fun <reified T> getApiService(): T {
        val env = AppEnv.getInstance().getEnv()
        val name = T::class.java.simpleName
        return allCache.getOrPut(env to name) {
            val serviceUrl = appEnv.getEnvServiceUrl(env)
            createService<T>(serviceUrl)!!
        } as T
    }

    private inline fun <reified T> createService(serviceUrl: String): T {
        return Retrofit.Builder()
            .baseUrl(serviceUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(T::class.java)
    }
}