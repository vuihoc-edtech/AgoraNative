package io.vuihoc.agora_native.data.model

data class NetworkRoomUser(
    var userUUID: String,
    var rtcUID: Int,
    var name: String,
    var avatarURL: String,
)