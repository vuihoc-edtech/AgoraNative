package io.vuihoc.agora_native.data

import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.common.board.DeviceState
import io.vuihoc.agora_native.data.model.UserInfo

/**
 * 提供App级别的KV存储
 */
class AppKVCenter {
    private var uInfo: UserInfo? = null
    private var token: String? = null
    private var sessionId: String = ""
    private var deviceState: DeviceState = DeviceState(camera = false, mic = true)

    //VH Configs
    val botUsersList = mutableListOf<String>()

    @ColorInt
    var whiteboardBackground = 0xFFFFFFFF.toInt()

    fun setToken(token: String) {
        this.token = token
    }

    fun getToken(): String? {
        return this.token
    }

    fun setUserInfo(userInfo: UserInfo) {
        uInfo = userInfo
    }

    fun getUserInfo(): UserInfo? {
        return uInfo
    }

    fun getSessionId(): String {
        return sessionId
    }

    fun updateSessionId(sessionId: String) {
        this.sessionId = sessionId
    }

    fun getDeviceStatePreference(): DeviceState {
        return deviceState
    }

    fun setDeviceStatePreference(deviceState: DeviceState) {
        this.deviceState = deviceState
    }

    fun getJoinEarly(): Int {
        return 5
    }

    companion object {
        @Volatile
        private var INSTANCE: AppKVCenter? = null

        fun getInstance(): AppKVCenter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppKVCenter().also { INSTANCE = it }
            }
        }
    }
}