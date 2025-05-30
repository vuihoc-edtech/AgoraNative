package io.vuihoc.agora_native.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import io.vuihoc.agora_native.common.board.DeviceState
import io.vuihoc.agora_native.data.model.UserInfo
import io.vuihoc.agora_native.data.model.UserInfoWithToken
import androidx.core.content.edit

/**
 * 提供App级别的KV存储
 */
class AppKVCenter {
    private lateinit var store: SharedPreferences
    private val gson = Gson()
    private val mockData = MockData()

    fun initStore(context: Context) {
        store = context.getSharedPreferences("flat_kv_data", Context.MODE_PRIVATE)
    }

    fun setToken(token: String) {
        store.edit {
            putString(KEY_LOGIN_TOKEN, token)
        }
    }

    fun getToken(): String? {
        if (mockData.isMockEnable()) {
            return mockData.getToken()
        }
        return store.getString(KEY_LOGIN_TOKEN, null)
    }

    fun setUserInfo(userInfo: UserInfo) {
        store.edit {
            putString(KEY_LOGIN_USER_INFO, gson.toJson(userInfo))
        }
    }

    fun getUserInfo(): UserInfo? {
        if (mockData.isMockEnable()) {
            return mockData.getUserInfo()
        }

        val userInfoJson = store.getString(KEY_LOGIN_USER_INFO, null)
        if (userInfoJson.isNullOrBlank())
            return null
        return gson.fromJson(userInfoJson, UserInfo::class.java)
    }

    fun getSessionId(): String {
        return store.getString(KEY_SESSION_ID, "") ?: ""
    }

    fun updateSessionId(sessionId: String) {
        store.edit {
            putString(KEY_SESSION_ID, sessionId)
        }
    }

    fun getDeviceStatePreference(): DeviceState {
        val preferenceJson = store.getString(KEY_DEVICE_STATE, null)
        return if (preferenceJson == null) {
            DeviceState(camera = false, mic = true)
        } else {
            gson.fromJson(preferenceJson, DeviceState::class.java)
        }
//        return DeviceState(camera = true, mic = true)
    }

    fun setDeviceStatePreference(deviceState: DeviceState) {
        store.edit {
            putString(KEY_DEVICE_STATE, gson.toJson(deviceState))
        }
    }

    fun getJoinEarly(): Int {
//        return 0
        return store.getInt(KEY_SERVER_JOIN_EARLY, 5)
    }

    companion object {
        const val KEY_LOGIN_TOKEN = "key_login_token"

        const val KEY_LOGIN_USER_INFO = "key_login_user_info"

        const val KEY_SESSION_ID = "key_session_id"

        const val KEY_DEVICE_STATE = "key_device_state"

        const val KEY_SERVER_JOIN_EARLY = "key_server_join_early"

        @Volatile
        private var INSTANCE: AppKVCenter? = null

        fun getInstance(): AppKVCenter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppKVCenter().also { INSTANCE = it }
            }
        }
    }

    class MockData {
        companion object {
            var mockEnable = false
            const val userInfoJson = """
                {"name":"冯利斌","avatar":"https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKUtPsvnxiaQtoHwaFPErfOrq1uN6wQ5UoMk7y2pPXcEibbVgTWBxeRrV80b4HkuJNB8o1STgaDXicFQ/132","userUUID":"3e092001-eb7e-4da5-a715-90452fde3194","token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyVVVJRCI6IjNlMDkyMDAxLWViN2UtNGRhNS1hNzE1LTkwNDUyZmRlMzE5NCIsImxvZ2luU291cmNlIjoiV2VDaGF0IiwiaWF0IjoxNjM2NDQ3Njg0LCJleHAiOjE2Mzg5NTMyODQsImlzcyI6ImZsYXQtc2VydmVyIn0.OvZCVBPPWDSUX8vwfTOSl81gnYRquLSVP2s5Xnslyrc"}
            """
        }

        private val gson = Gson()

        fun isMockEnable(): Boolean {
            return mockEnable
        }

        fun getUserInfo(): UserInfo {
            return gson.fromJson(userInfoJson, UserInfo::class.java)
        }

        fun getToken(): String {
            val withToken = gson.fromJson(userInfoJson, UserInfoWithToken::class.java)
            return withToken.token
        }
    }
}