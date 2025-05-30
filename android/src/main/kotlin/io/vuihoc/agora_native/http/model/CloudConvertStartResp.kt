package io.vuihoc.agora_native.http.model

import io.vuihoc.agora_native.data.model.ResourceType

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