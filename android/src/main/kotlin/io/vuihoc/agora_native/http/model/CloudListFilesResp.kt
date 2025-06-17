package io.vuihoc.agora_native.http.model

import io.vuihoc.agora_native.data.model.CloudFile

data class CloudListFilesResp(
    val totalUsage: Long,
    val files: List<CloudFile>,
    val canCreateDirectory: Boolean,
)

