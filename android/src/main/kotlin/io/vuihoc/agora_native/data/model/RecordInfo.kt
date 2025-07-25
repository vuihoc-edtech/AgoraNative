package io.vuihoc.agora_native.data.model

data class RecordInfo(
    val title: String,
    val ownerUUID: String,
    val roomType: RoomType,
    val region: String,
    val whiteboardRoomToken: String,
    val whiteboardRoomUUID: String,
    val rtmToken: String,
    val recordInfo: List<RecordItem>,
)

data class RecordItem(
    val beginTime: Long,
    val endTime: Long,
    val videoURL: String,
)