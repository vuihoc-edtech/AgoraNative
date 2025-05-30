package io.vuihoc.agora_native.data.repository

import io.vuihoc.agora_native.data.AppKVCenter
import io.vuihoc.agora_native.data.model.UserInfo


class UserRepository(
    private val appKVCenter: AppKVCenter = AppKVCenter.getInstance(),
) {
    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository().also { INSTANCE = it }
            }
        }
    }

    fun getUserInfo(): UserInfo? {
        return appKVCenter.getUserInfo()
    }

    fun getUsername(): String {
        return getUserInfo()!!.name
    }

    fun getUserAvatar(): String {
        return getUserInfo()!!.avatar
    }

    fun getUserUUID(): String {
        return getUserInfo()!!.uuid
    }

}
