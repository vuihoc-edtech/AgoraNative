package io.vuihoc.agora_native.http.model

data class CloudFileRenameReq(
    val fileUUID: String,
    val newName: String,
)
