package io.vuihoc.agora_native.http.model

data class CloudUploadTempFileStartReq(
    val fileName: String,
    val fileSize: Long,
)