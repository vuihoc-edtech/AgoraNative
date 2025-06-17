package io.vuihoc.agora_native.http.model

data class CloudListFilesReq(
    val page: Int,
    val directoryPath: String,
    val size: Int,
    val order: String,
)