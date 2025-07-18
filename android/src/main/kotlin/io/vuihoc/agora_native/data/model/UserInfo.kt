package io.vuihoc.agora_native.data.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    val name: String,
    val avatar: String,
    @SerializedName("userUUID") val uuid: String,
    val hasPhone: Boolean = false,
    val hasPassword: Boolean = false,
    val token: String = ""
)