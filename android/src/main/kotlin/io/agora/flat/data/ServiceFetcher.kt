package io.agora.flat.data

import io.agora.flat.http.api.CloudRecordService
import io.agora.flat.http.api.CloudStorageServiceV2
import io.agora.flat.http.api.MessageService
import io.agora.flat.http.api.MiscService
import io.agora.flat.http.api.RoomService
import io.agora.flat.http.api.UserService
import io.agora.flat.http.interceptor.HeaderInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Fetch room service by uuid
 *
 * For joining rooms between different regions
 */
class ServiceFetcher(
    private val appEnv: AppEnv = AppEnv.getInstance()
) {
    companion object {

        @Volatile
        private var INSTANCE: ServiceFetcher? = null

        fun getInstance(): ServiceFetcher {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ServiceFetcher().also {
                    INSTANCE = it
                    it.client = OkHttpClient.Builder()
                        .addInterceptor(HeaderInterceptor())
                        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                        .build()
                }
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

    private lateinit var client: OkHttpClient

    private val allCache = mutableMapOf<Pair<String, String>, Any>()

    fun fetchRoomService(): RoomService {
        return getApiService<RoomService>(appEnv.flatServiceUrl)
    }

    fun fetchUserService(): UserService {
        return getApiService<UserService>(appEnv.flatServiceUrl)
    }

    fun fetchCloudRecordService(): CloudRecordService {
        return getApiService<CloudRecordService>(appEnv.flatServiceUrl)
    }

    fun fetchMessageService(): MessageService {
        return getApiService<MessageService>("https://api.agora.io/dev/")
    }

    fun fetchMiscService(): MiscService {
        return getApiService<MiscService>(appEnv.flatServiceUrl)
    }

    fun fetchCloudStorageServiceV2(): CloudStorageServiceV2 {
        return getApiService<CloudStorageServiceV2>(appEnv.flatServiceUrl)
    }

    private inline fun <reified T> getApiService( baseUrl: String? = null): T {
        val env = AppEnv.getInstance().getEnv()
        val name = T::class.java.simpleName
        return allCache.getOrPut(env to name) {
            val serviceUrl = baseUrl ?: appEnv.getEnvServiceUrl(env)
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