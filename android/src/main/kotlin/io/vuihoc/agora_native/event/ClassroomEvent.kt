package io.vuihoc.agora_native.event

import io.vuihoc.agora_native.util.ContentInfo

sealed class ClassroomEvent : Event()

data class RequestDeviceSent(
    val mic: Boolean? = null,
    val camera: Boolean? = null
) : ClassroomEvent()

object RequestMuteAllSent : ClassroomEvent()

data class RequestDeviceReceived(
    val mic: Boolean? = null,
    val camera: Boolean? = null
) : ClassroomEvent()

data class RequestDeviceResponseReceived(
    val username: String,
    val mic: Boolean? = null,
    val camera: Boolean? = null
) : ClassroomEvent()

data class NotifyDeviceOffReceived(
    val mic: Boolean? = null,
    val camera: Boolean? = null,
) : ClassroomEvent()

data class RewardReceived(
    val userUUID: String,
) : ClassroomEvent()

object RemoteLoginEvent : ClassroomEvent()

data class TakePhotoEvent(val info: ContentInfo) : ClassroomEvent()

data class ExpirationEvent(
    val roomLevel: Int,
    val expireAt: Long,
    val leftMinutes: Int,
) : ClassroomEvent()

object RoomKickedEvent : ClassroomEvent()