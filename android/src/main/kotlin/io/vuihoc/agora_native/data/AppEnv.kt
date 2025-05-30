package io.vuihoc.agora_native.data

/**
 * 应用内切换配置
 */
class AppEnv {
    companion object {
        @Volatile
        private var INSTANCE: AppEnv? = null

        fun getInstance(): AppEnv {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppEnv().also { INSTANCE = it }
            }
        }
    }

    val envItem: EnvItem = EnvItem(
        agoraAppId = "839e5d402e2a4371bd4a788bbab4f2d8",
        serviceUrl = "https://dev-class-api.rinoedu.ai",
        ossKey = "LTAI5tRTgaUQqSs5SDUvLmBA",
        whiteAppId = "Q9CKoASEEfCMH5n9aKKKyw/wsn4bnq3RHQkzA",
    )

    fun getEnv(): String {
        return "vh"
    }

    fun getEnvServiceUrl(): String {
        return envItem.serviceUrl
    }

    val flatServiceUrl get() = envItem.serviceUrl
    val agoraAppId get() = envItem.agoraAppId
    val ossKey get() = envItem.ossKey
    val whiteAppId get() = envItem.whiteAppId
    val region get() = envItem.region

    data class EnvItem(
        var agoraAppId: String,
        var serviceUrl: String,
        var ossKey: String,
        var whiteAppId: String? = null,
        var region: String = "sg",
    )
}