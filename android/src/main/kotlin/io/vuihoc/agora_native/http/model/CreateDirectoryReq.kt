package io.vuihoc.agora_native.http.model

data class CreateDirectoryReq(
    val parentDirectoryPath: String,
    val directoryName: String,
)