package io.vuihoc.agora_native.data.model

data class MessageQueryHistoryResp(
    val result: String,
    val offset: Int,
    val limit: Int,
    val order: String,
    val location: String,
)
