package io.vuihoc.agora_native.data

import io.vuihoc.agora_native.http.api.CloudRecordService
import io.vuihoc.agora_native.http.api.CloudStorageServiceV2
import io.vuihoc.agora_native.http.api.MessageService
import io.vuihoc.agora_native.http.api.MiscService
import io.vuihoc.agora_native.http.api.RoomService
import io.vuihoc.agora_native.http.interceptor.HeaderInterceptor
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
    }

    private lateinit var client: OkHttpClient

    private val allCache = mutableMapOf<Pair<String, String>, Any>()

    fun fetchRoomService(): RoomService {
        return getApiService<RoomService>(appEnv.flatServiceUrl)
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
            val serviceUrl = baseUrl ?: appEnv.getEnvServiceUrl()
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