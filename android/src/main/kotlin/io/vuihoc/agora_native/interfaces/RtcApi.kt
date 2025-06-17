package io.vuihoc.agora_native.interfaces

import io.vuihoc.agora_native.common.rtc.RtcEvent
import io.vuihoc.agora_native.common.rtc.RtcJoinOptions
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.flow.Flow

interface RtcApi {
    fun joinChannel(options: RtcJoinOptions): Int

    fun leaveChannel(): Int

    fun enableLocalVideo(enabled: Boolean)

    fun enableLocalAudio(enabled: Boolean)

    fun setupLocalVideo(local: VideoCanvas)

    fun setupRemoteVideo(remote: VideoCanvas)

    /**
     * enable local audio or video
     */
    fun updateLocalStream(audio: Boolean, video: Boolean)

    fun updateRemoteStream(rtcUid: Int, audio: Boolean, video: Boolean)

    fun observeRtcEvent(): Flow<RtcEvent>

    companion object {
        const val MAX_CAPACITY = 17
    }
}