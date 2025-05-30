package io.vuihoc.agora_native.data.model

data class RecordReq(
    val roomUUID: String,
    val agoraParams: AgoraRecordParams,
)