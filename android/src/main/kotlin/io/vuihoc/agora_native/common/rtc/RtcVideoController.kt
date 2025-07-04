package io.vuihoc.agora_native.common.rtc

import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.vuihoc.agora_native.interfaces.RtcApi
import io.agora.rtc2.video.VideoCanvas
import kotlin.collections.set


class RtcVideoController(private val rtcApi: RtcApi) {
    private var textureMap = HashMap<Int, TextureView>()

    var shareScreenContainer: FrameLayout? = null
    private var localUid: Int = 0
    private var shareScreenUid: Int = 0
    private var fullScreenUid: Int = 0

    fun setupUid(uid: Int, ssUid: Int) {
        localUid = uid
        shareScreenUid = ssUid
    }

    fun enterFullScreen(uid: Int) {
        this.fullScreenUid = uid
    }

    fun exitFullScreen() {
        fullScreenUid = 0
    }

    fun updateFullScreenVideo(videoContainer: FrameLayout, uid: Int) {
        if (fullScreenUid == uid) {
            setupUserVideo(videoContainer, uid)
        }
    }

    fun setupUserVideo(container: FrameLayout, uid: Int) {
        if (uid == 0) {
            container.removeAllViews()
            return
        }
        if (textureMap[uid] == null) {
            textureMap[uid] = TextureView(container.context)
        }

        val textureView = textureMap[uid]!!
        if (textureView.parent == container) {
            setupVideo(textureView, uid)
        } else {
            (textureView.parent as? FrameLayout)?.removeAllViews()

            with(container) {
                removeAllViews()
                addView(textureView, generateLayoutParams())
                setupVideo(textureView, uid)
            }
        }
    }

    private fun generateLayoutParams() = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )

//    private fun releaseVideo(uid: Int) {
//        setupVideoByVideoCanvas(uid, VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid))
//    }

    private fun setupVideo(textureView: View, uid: Int) {
        setupVideoByVideoCanvas(uid, VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    private fun setupVideoByVideoCanvas(uid: Int, videoCanvas: VideoCanvas) {
        if (uid == localUid) {
            rtcApi.setupLocalVideo(videoCanvas)
        } else {
            rtcApi.setupRemoteVideo(videoCanvas)
        }
    }

    fun handleOffline(uid: Int) {
        if (uid == shareScreenUid) {
            shareScreenContainer?.run {
                removeAllViews()
                isVisible = false
            }
        }
    }

    fun handlerJoined(uid: Int) {
        if (uid == shareScreenUid) {
            shareScreenContainer?.run {
                val textureView = TextureView(context)
                rtcApi.setupRemoteVideo(VideoCanvas(textureView, VideoCanvas.RENDER_MODE_FIT, uid))
                addView(textureView, generateLayoutParams())
                isVisible = true
            }
        }
    }
}