package io.vuihoc.agora_native.common.board

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.herewhite.sdk.WhiteSdk
import com.herewhite.sdk.domain.Promise
import com.herewhite.sdk.domain.RoomPhase
import com.herewhite.sdk.domain.SDKError
import com.herewhite.sdk.domain.Scene
import com.herewhite.sdk.domain.WindowAppParam
import com.herewhite.sdk.domain.WindowPrefersColorScheme.Dark
import com.herewhite.sdk.domain.WindowPrefersColorScheme.Light
import io.agora.board.fast.FastException
import io.agora.board.fast.FastRoom
import io.agora.board.fast.FastRoomListener
import io.agora.board.fast.Fastboard
import io.agora.board.fast.FastboardView
import io.agora.board.fast.extension.FastResource
import io.agora.board.fast.model.FastAppliance
import io.agora.board.fast.model.FastRegion
import io.agora.board.fast.model.FastRoomOptions
import io.agora.board.fast.model.FastUserPayload
import io.agora.board.fast.ui.RoomControllerGroup
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.common.FlatBoardException
import io.vuihoc.agora_native.common.rtc.AgoraRtc
import io.vuihoc.agora_native.data.AppEnv
import io.vuihoc.agora_native.data.AppKVCenter
import io.vuihoc.agora_native.data.repository.UserRepository
import io.vuihoc.agora_native.interfaces.BoardRoom
import io.vuihoc.agora_native.interfaces.SyncedClassState
import io.vuihoc.agora_native.util.dp
import io.vuihoc.agora_native.util.px2dp
import io.vuihoc.agora_native.util.toRgbHex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class AgoraBoardRoom(
    private val userRepository: UserRepository = UserRepository.getInstance(),
    val syncedClassState: SyncedClassState = WhiteSyncedState(),
    private val appEnv: AppEnv = AppEnv.getInstance(),
    ) : BoardRoom {
    private lateinit var fastboard: Fastboard
    private lateinit var fastboardView: FastboardView

    private var fastRoom: FastRoom? = null
    private var darkMode: Boolean = false
    private var rootRoomController: RoomControllerGroup? = null
    private var boardPhase = MutableStateFlow<BoardPhase>(BoardPhase.Init)
    private var boardError = MutableStateFlow<BoardError?>(null)
    private val activityContext: Context by lazy { fastboardView.context }
    private val flatNetlessUA: List<String> by lazy {
        listOf(
            "fastboard/${Fastboard.VERSION}",
            "FLAT/NETLESS@2.12.0",
        )
    }

    override fun setupView(fastboardView: FastboardView) {
        this.fastboardView = fastboardView
        this.fastboard = fastboardView.fastboard
    }

    override fun setRoomController(rootRoomController: RoomControllerGroup) {
        this.rootRoomController = rootRoomController
        fastRoom?.rootRoomController = rootRoomController
    }

    @SuppressLint("SetJavaScriptEnabled")
    override suspend fun join(
        roomUUID: String,
        roomToken: String,
        region: String,
        writable: Boolean
    ) {
        val fastRoomOptions = FastRoomOptions(
            appEnv.whiteAppId,
            roomUUID,
            roomToken,
            userRepository.getUserUUID(),
            region.toFastRegion(),
            writable
        ).apply {
            userPayload = FastUserPayload(userRepository.getUsername())
        }

        fastRoomOptions.sdkConfiguration = fastRoomOptions.sdkConfiguration.apply {
            isLog = true
            netlessUA = flatNetlessUA
            isEnableSyncedStore = true
        }

        fastRoomOptions.roomParams = fastRoomOptions.roomParams.apply {
            windowParams.prefersColorScheme = if (darkMode) Dark else Light
            windowParams.collectorStyles = getCollectorStyle()
            disableEraseImage = true
        }

        fastRoom = fastboard.createFastRoom(fastRoomOptions)
        fastboardView.whiteboardView.settings.javaScriptEnabled = true
        fastRoom?.addListener(object : FastRoomListener {
            override fun onRoomPhaseChanged(phase: RoomPhase) {
                Log.d(TAG,"[BOARD] room phase change to ${phase.name}")
                when (phase) {
                    RoomPhase.connecting -> boardPhase.value = BoardPhase.Connecting
                    RoomPhase.connected -> boardPhase.value = BoardPhase.Connected
                    RoomPhase.disconnected -> boardPhase.value = BoardPhase.Disconnected
                    else -> {}
                }
            }

            override fun onRoomReadyChanged(fastRoom: FastRoom) {
                Log.d(TAG,"[BOARD] room ready changed ${fastRoom.isReady}")
                if (syncedClassState is WhiteSyncedState && fastRoom.isReady) {
                    syncedClassState.resetRoom(fastRoom)
                }

                if(fastRoom.isReady) {
                    setBoardBackground()
                }
            }

            override fun onFastError(error: FastException) {
                if (error.code == FastException.ROOM_KICKED) {
                    boardError.value = BoardError.Kicked
                } else {
                    // boardError.value = BoardError.Unknown(error.message ?: "Unknown error")
                }
            }
        })

        rootRoomController?.let {
            fastRoom?.rootRoomController = it
            updateRoomController(writable)
        }

        fastRoom?.setErrorHandler {
           Log.d(TAG,"[BOARD] error ${it.message}")
        }

        val fastResource = object : FastResource() {
            override fun getBackgroundColor(darkMode: Boolean): Int {
                return ContextCompat.getColor(
                    activityContext,
                    if (darkMode) R.color.flat_gray_7 else R.color.flat_blue_0
                )
            }

            override fun getBoardBackgroundColor(darkMode: Boolean): Int {
                return ContextCompat.getColor(activityContext, R.color.flat_day_night_background)
            }

            override fun createApplianceBackground(darkMode: Boolean): Drawable? {
                return ContextCompat.getDrawable(activityContext, R.drawable.ic_class_room_icon_bg)
            }

            override fun getIconColor(darkMode: Boolean): ColorStateList? {
                return ContextCompat.getColorStateList(activityContext, R.color.color_class_room_icon)
            }

            override fun getLayoutBackground(darkMode: Boolean): Drawable? {
                return ContextCompat.getDrawable(activityContext, R.drawable.shape_gray_border_round_8_bg)
            }
        }
        fastRoom?.setResource(fastResource)
        setDarkMode(darkMode)
//        WhiteSdk.setAudioMixerBridge(AgoraRtc.getInstance())
        fastRoom?.join()
        fastboard.setWhiteboardRatio(null)

    }

    private fun setBoardBackground() {
        try {
            val color = AppKVCenter.getInstance().whiteboardBackground.toRgbHex()
            Log.d(TAG, "set background Board $color")
            val js = """
                    (function() {
                        const stage = document.getElementsByClassName("telebox-manager-stage")[0];
                        if (stage) {
                            stage.style.backgroundColor = "$color";
                            console.log("$TAG: background color set directly");
                        } else {
                            console.log("$TAG: stage not found");
                        }
                    })();
                """.trimIndent()
            fastboardView.whiteboardView.evaluateJavascript(js, null)
        } catch (e: Exception) {
            Log.d(TAG, "Change background error")
        }

    }

    private fun getCollectorStyle(): HashMap<String, String> {
        val styleMap = HashMap<String, String>()
        styleMap["top"] = "${activityContext.dp(R.dimen.flat_gap_2_0)}px"
        styleMap["right"] = "${activityContext.dp(R.dimen.flat_gap_2_0)}px"
        styleMap["width"] = "${activityContext.dp(R.dimen.room_class_button_area_size)}px"
        styleMap["height"] = "${activityContext.dp(R.dimen.room_class_button_area_size)}px"
        styleMap["position"] = "fixed"
        styleMap["border-radius"] = "8px"
        styleMap["border"] = "1px solid rgba(0,0,0,.15)"
        return styleMap
    }

    override fun setDarkMode(dark: Boolean) {
        Log.d(TAG,"[BOARD] set dark mode $dark, fastboard ${::fastboard.isInitialized}")
        this.darkMode = dark
        if (::fastboard.isInitialized) {
            val fastStyle = fastboard.fastStyle.apply { isDarkMode = dark }
            fastRoom?.fastStyle = fastStyle
        }
    }

    override fun release() {
        fastRoom?.destroy()
        fastRoom = null
    }

    override suspend fun setWritable(writable: Boolean): Boolean = suspendCoroutine {
        Log.d(TAG,"[BoardRoom] set writable $writable, when isWritable ${fastRoom?.isWritable}")
        if (fastRoom?.isWritable == writable) {
            it.resume(writable)
            return@suspendCoroutine
        }
        fastRoom?.room?.setWritable(writable, object : Promise<Boolean> {
            override fun then(success: Boolean) {
                Log.d(TAG,"[BoardRoom] set writable result $success")
                it.resume(success)
            }

            override fun catchEx(t: SDKError) {
                Log.d(TAG,"[BoardRoom] set writable error ${t.jsStack}")
                it.resumeWithException(t)
            }
        }) ?: it.resumeWithException(FlatBoardException("[BoardRoom] room not ready"))
    }

    override suspend fun setAllowDraw(allow: Boolean) {
        Log.d(TAG,"[BoardRoom] set allow draw $allow, when isWritable ${fastRoom?.isWritable}")
        if (fastRoom?.isWritable == true) {
            fastRoom?.room?.disableOperations(!allow)
            fastRoom?.room?.disableWindowOperation(!allow)
        }
        fastboardView.post { updateRoomController(allow) }
    }

    private fun updateRoomController(allow: Boolean) {
        if (allow) {
            fastRoom?.rootRoomController?.show()
        } else {
            fastRoom?.rootRoomController?.hide()
        }
    }

    override fun hideAllOverlay() {
        fastRoom?.overlayManger?.hideAll()
    }

    override fun insertImage(imageUrl: String, w: Int, h: Int) {
        val scale = fastRoom?.room?.roomState?.cameraState?.scale ?: 1.0
        // Images are limited to a maximum of 0.4 times the width of the screen.
        val limitWidth = (activityContext.px2dp(fastboardView.width) / scale * 0.4).toInt()

        val targetW: Int
        val targetH: Int
        if (w > limitWidth) {
            targetW = limitWidth
            targetH = limitWidth * h / w
        } else {
            targetW = w
            targetH = h
        }

        fastRoom?.insertImage(imageUrl, targetW, targetH)
        // switch to SELECTOR when insertImage
        fastRoom?.setAppliance(FastAppliance.SELECTOR)
    }

    override fun insertPpt(dir: String, scenes: List<Scene>, title: String) {
        val param = WindowAppParam.createSlideApp(dir, scenes.toTypedArray(), title)
        fastRoom?.room?.addApp(param, null)
    }

    override fun insertProjectorPpt(taskUuid: String, prefixUrl: String, title: String) {
        val param = WindowAppParam.createSlideApp(taskUuid, prefixUrl, title)
        fastRoom?.room?.addApp(param, null)
    }

    override fun insertVideo(videoUrl: String, title: String) {
        fastRoom?.insertVideo(videoUrl, title)
    }

    override fun insertApp(kind: String) {
        fastRoom?.room?.addApp(WindowAppParam(kind, null, null), null)
    }

    override fun observeRoomPhase(): Flow<BoardPhase> {
        return boardPhase.asStateFlow()
    }

    override fun observeRoomError(): Flow<BoardError> {
        return boardError.asStateFlow().filterNotNull()
    }

    private fun String.toFastRegion(): FastRegion {
        val region = FastRegion.values().find { it.name.lowercase().replace('_', '-') == this }
        return region ?: FastRegion.CN_HZ
    }

    companion object {
        const val TAG = "VHLog AgoraBoardRoom"
    }
}