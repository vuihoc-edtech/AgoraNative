package io.vuihoc.agora_native.data.model

import com.google.gson.annotations.SerializedName

data class RoomCount(
    @SerializedName("alreadyJoinedRoomCount")
    val count: Int,
)
