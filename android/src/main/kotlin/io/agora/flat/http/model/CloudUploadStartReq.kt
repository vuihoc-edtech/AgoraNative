package io.agora.flat.http.model

data class CloudUploadStartReq(
    val fileName: String,
    val fileSize: Long,
    val targetDirectoryPath: String,
    val convertType: String? = null,
)