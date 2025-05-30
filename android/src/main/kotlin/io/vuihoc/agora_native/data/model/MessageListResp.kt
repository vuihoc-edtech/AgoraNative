package io.vuihoc.agora_native.data.model

data class MessageListResp(
    val result: String,
    val code: String,
    val messages: List<RtmQueryMessage>,
    val request_id: String,
)
