package io.vuihoc.agora_native.http.api

import io.vuihoc.agora_native.data.model.BaseReq
import io.vuihoc.agora_native.data.model.BaseResp
import io.vuihoc.agora_native.data.model.CancelRoomReq
import io.vuihoc.agora_native.data.model.JoinRoomReq
import io.vuihoc.agora_native.data.model.NetworkRoomUser
import io.vuihoc.agora_native.data.model.PeriodicSubRoom
import io.vuihoc.agora_native.data.model.PeriodicSubRoomReq
import io.vuihoc.agora_native.data.model.PureRoomReq
import io.vuihoc.agora_native.data.model.RespNoData
import io.vuihoc.agora_native.data.model.RoomCreateReq
import io.vuihoc.agora_native.data.model.RoomCreateRespData
import io.vuihoc.agora_native.data.model.RoomDetailOrdinary
import io.vuihoc.agora_native.data.model.RoomDetailOrdinaryReq
import io.vuihoc.agora_native.data.model.RoomDetailPeriodic
import io.vuihoc.agora_native.data.model.RoomDetailPeriodicReq
import io.vuihoc.agora_native.data.model.RoomInfo
import io.vuihoc.agora_native.data.model.RoomPlayInfo
import io.vuihoc.agora_native.data.model.RoomUsersReq
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface RoomService {
    @POST("v1/room/info/ordinary")
    fun getOrdinaryRoomInfo(
        @Body detailOrdinaryReq: RoomDetailOrdinaryReq,
    ): Call<BaseResp<RoomDetailOrdinary>>

    @POST("v1/room/join")
    fun joinRoom(
        @Body joinRoomReq: JoinRoomReq,
    ): Call<BaseResp<RoomPlayInfo>>

    // 获取房间内所有用户的信息
    @POST("v1/room/info/users")
    fun getRoomUsers(
        @Body roomUsersReq: RoomUsersReq,
    ): Call<BaseResp<Map<String, NetworkRoomUser>>>

    @POST("v1/room/update-status/started")
    fun startRoomClass(
        @Body req: PureRoomReq,
    ): Call<BaseResp<RespNoData>>

    @POST("v1/room/update-status/paused")
    fun pauseRoomClass(
        @Body req: PureRoomReq,
    ): Call<BaseResp<RespNoData>>

    @POST("v1/room/update-status/stopped")
    fun stopRoomClass(
        @Body req: PureRoomReq,
    ): Call<BaseResp<RespNoData>>
}