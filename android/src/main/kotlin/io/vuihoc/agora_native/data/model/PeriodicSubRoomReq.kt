package io.vuihoc.agora_native.data.model

data class PeriodicSubRoomReq(
    val periodicUUID: String,
    val roomUUID: String,
    val needOtherRoomTimeInfo: Boolean?,
)
