package io.vuihoc.agora_native.data.model

data class PeriodicSubRoom(
    val roomInfo: RoomInfo,
    val previousPeriodicRoomBeginTime: Long?,
    val nextPeriodicRoomEndTime: Long?,
    val count: Int,
    val docs: List<RoomDocs>,
)
