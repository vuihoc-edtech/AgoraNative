package io.vuihoc.agora_native.http.model

data class CloudUploadStartResp(
    val fileUUID: String,
    val ossDomain: String,
    val ossFilePath: String,
    val policy: String,
    val signature: String,
    val convertType: String? = null,
)