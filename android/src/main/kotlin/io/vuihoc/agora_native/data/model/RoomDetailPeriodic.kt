package io.vuihoc.agora_native.data.model

/**
 * 周期性房间详情
 */
data class RoomDetailPeriodic(
    val periodic: RoomPeriodic,
    val rooms: ArrayList<RoomInfo>,
)
