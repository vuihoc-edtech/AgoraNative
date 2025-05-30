package io.vuihoc.agora_native.data.model

data class RecordAcquireReq(
    val roomUUID: String,
    val agoraData: RecordAcquireReqData,
)

data class RecordAcquireReqData(
    val clientRequest: RecordAcquireReqDataClientRequest,
)

data class RecordAcquireReqDataClientRequest(
    val resourceExpiredHour: Int,
    val scene: Int,
)