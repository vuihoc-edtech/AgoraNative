package io.vuihoc.agora_native.data.model

data class RoomDocs(
    val docType: DocsType,
    val docUUID: String,
    val isPreload: Boolean,
)