package io.agora.flat.http.model

import io.agora.flat.data.model.ResourceType

data class CloudConvertStartResp(
    val resourceType: ResourceType,
    val whiteboardConvert: WhiteboardConvert?,
    val whiteboardProjector: WhiteboardProjector?,
)

data class WhiteboardConvert(
    val taskUUID: String,
    val taskToken: String,
)

data class WhiteboardProjector(
    val taskUUID: String,
    val taskToken: String,
)