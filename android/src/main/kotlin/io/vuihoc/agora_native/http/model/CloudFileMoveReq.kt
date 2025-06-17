package io.vuihoc.agora_native.http.model

data class CloudFileMoveReq(
    val uuids: List<String>,
    val targetDirectoryPath: String,
)
