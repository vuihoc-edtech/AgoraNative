package io.agora.flat.data.model

data class RecordQueryRespData(
    val sid: String,
    val resourceId: String,
    val serverResponse: QueryServerResponse,
)

data class QueryServerResponse(
    val status: Int,
    val fileList: String,
    // val fileListMode: String,
    val sliceStartTime: Long,
)