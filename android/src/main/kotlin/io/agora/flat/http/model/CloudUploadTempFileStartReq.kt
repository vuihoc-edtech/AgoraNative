package io.agora.flat.http.model

data class CloudUploadTempFileStartReq(
    val fileName: String,
    val fileSize: Long,
)