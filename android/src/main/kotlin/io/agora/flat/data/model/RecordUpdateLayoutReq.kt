package io.agora.flat.data.model

data class RecordUpdateLayoutReq(
    val roomUUID: String,
    val agoraParams: AgoraRecordParams,
    val agoraData: AgoraRecordUpdateLayoutData,
)

data class AgoraRecordUpdateLayoutData(
    val clientRequest: UpdateLayoutClientRequest,
)

data class UpdateLayoutClientRequest(
    val maxResolutionUid: String? = null,
    val mixedVideoLayout: Int = 3,
    val backgroundColor: String = "#FFFFFF",
    val layoutConfig: List<LayoutConfig>? = null,
    val backgroundConfig: List<BackgroundConfig>? = null,
)