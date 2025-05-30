package io.vuihoc.agora_native.data.model

import com.google.gson.annotations.SerializedName

data class BaseResp<T>(
    @SerializedName("status") val status: Int,
    @SerializedName("code") val code: Int?,
    @SerializedName("data") val data: T,
)

