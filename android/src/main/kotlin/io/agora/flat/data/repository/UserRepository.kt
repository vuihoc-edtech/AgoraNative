package io.agora.flat.data.repository

import io.agora.flat.data.AppKVCenter
import io.agora.flat.data.model.UserInfo


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
