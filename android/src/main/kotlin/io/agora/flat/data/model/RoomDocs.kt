package io.agora.flat.data.model

data class RoomDocs(
    val docType: DocsType,
    val docUUID: String,
    val isPreload: Boolean,
)