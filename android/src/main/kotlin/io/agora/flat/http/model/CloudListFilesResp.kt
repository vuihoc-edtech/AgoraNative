package io.agora.flat.http.model

import io.agora.flat.data.model.CloudFile

data class CloudListFilesResp(
    val totalUsage: Long,
    val files: List<CloudFile>,
    val canCreateDirectory: Boolean,
)

