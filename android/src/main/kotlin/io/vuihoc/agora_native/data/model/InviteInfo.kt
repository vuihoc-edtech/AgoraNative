package io.vuihoc.agora_native.data.model

data class InviteInfo(
    val username: String,
    val roomTitle: String,
    val link: String,
    val roomUuid: String,
    val beginTime: Long,
    val endTime: Long,
    val isPmi: Boolean
)
