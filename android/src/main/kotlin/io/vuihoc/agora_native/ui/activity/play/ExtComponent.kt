package io.vuihoc.agora_native.ui.activity.play

import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import io.vuihoc.agora_native.Constants
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.common.rtc.NetworkQuality
import io.vuihoc.agora_native.common.rtc.RtcEvent
import io.vuihoc.agora_native.data.error.FlatErrorHandler
import io.vuihoc.agora_native.data.model.RoomStatus
import io.vuihoc.agora_native.databinding.ComponentExtensionBinding
import io.vuihoc.agora_native.databinding.ComponentRoomStateBinding
import io.vuihoc.agora_native.event.RemoteLoginEvent
import io.vuihoc.agora_native.event.RoomKickedEvent
import io.vuihoc.agora_native.ui.manager.RoomOverlayManager
import io.vuihoc.agora_native.ui.util.UiMessage
import io.vuihoc.agora_native.ui.view.RoomExitDialog
import io.vuihoc.agora_native.ui.view.TimeStateData
import io.vuihoc.agora_native.util.delayAndFinish
import io.vuihoc.agora_native.util.isDarkMode
import io.vuihoc.agora_native.util.showToast
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * display common loading, toast, dialog, global layout change.
 */
class ExtComponent(
    activity: ClassRoomActivity,
    rootView: FrameLayout,
    private val roomStateContainer: FrameLayout,
) : BaseComponent(activity, rootView) {
    private lateinit var extensionBinding: ComponentExtensionBinding
    private lateinit var roomStateBinding: ComponentRoomStateBinding

    private val viewModel: ExtensionViewModel by activity.viewModels()
    private val classRoomViewModel: ClassRoomViewModel by activity.viewModels()

    override fun onCreate(owner: LifecycleOwner) {
        initView()
        observeState()
    }

    private fun initView() {
        extensionBinding =
            ComponentExtensionBinding.inflate(activity.layoutInflater, rootView, true)
        roomStateBinding =
            ComponentRoomStateBinding.inflate(activity.layoutInflater, roomStateContainer, true)
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                classRoomViewModel.loginThirdParty()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect {
                    showLoading(it.loading)
                    it.error?.run {
                        handleErrorMessage(it.error)
                        viewModel.clearError()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                classRoomViewModel.state.filterNotNull().collect {
                    if (it.roomStatus == RoomStatus.Stopped) {
                        showRoomExitDialog(activity.getString(R.string.exit_room_stopped_message))
                    }

                    // TODO current version does not have an end time limit
                    roomStateBinding.timeStateLayout.updateTimeStateData(
                        TimeStateData(
                            it.beginTime,
                            Long.MAX_VALUE,
                            10 * 60 * 1000,
                        )
                    )
                }
            }
        }

        lifecycleScope.launch {


            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                classRoomViewModel.classroomEvent.collect { event ->
                    when (event) {
                        RemoteLoginEvent -> {
                            showRoomExitDialog(activity.getString(R.string.exit_remote_login_message))
                        }

                        RoomKickedEvent -> {
                            showRoomExitDialog(activity.getString(R.string.exit_room_stopped_message))
                        }

                        else -> {}
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                classRoomViewModel.rtcEvent.collect { event ->
                    when (event) {
                        is RtcEvent.NetworkStatus -> {
                            when (event.quality) {
                                NetworkQuality.Unknown, NetworkQuality.Excellent -> {
                                    roomStateBinding.networkStateIcon.setColorFilter(
                                        ContextCompat.getColor(
                                            activity,
                                            R.color.flat_green_6
                                        )
                                    )
                                }

                                NetworkQuality.Good -> {
                                    roomStateBinding.networkStateIcon.setColorFilter(
                                        ContextCompat.getColor(
                                            activity,
                                            R.color.flat_yellow_6
                                        )
                                    )
                                }

                                NetworkQuality.Bad -> {
                                    roomStateBinding.networkStateIcon.setColorFilter(
                                        ContextCompat.getColor(
                                            activity,
                                            R.color.flat_red_6
                                        )
                                    )
                                }
                            }
                        }

                        is RtcEvent.LastmileDelay -> {
                            roomStateBinding.networkDelay.text =
                                activity.getString(R.string.room_class_network_delay, event.delay)
                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }

    private fun handleErrorMessage(error: UiMessage) {
        if (error.exception == null) {
            activity.showToast(error.text)
        } else {
            showRoomExitDialog(FlatErrorHandler.getErrorStr(activity, error.exception))
        }
    }

    private fun showLoading(show: Boolean) {
        extensionBinding.loadingLayout.isVisible = show
        if (show) {
            extensionBinding.loadingView.load(
                if (activity.isDarkMode()) R.raw.loading_dark else R.raw.loading_light,
                gifImageLoader,
            ) {
                crossfade(true)
            }
        }
    }

    private val gifImageLoader = ImageLoader.Builder(activity).apply {
        componentRegistry {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                add(ImageDecoderDecoder(activity))
            } else {
                add(GifDecoder())
            }
        }
    }.build()

    private fun showRoomExitDialog(message: String) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        val dialog = RoomExitDialog().apply {
            arguments = Bundle().apply {
                putString(Constants.IntentKey.MESSAGE, message)
            }
        }
        dialog.setListener { activity.delayAndFinish(250) }
        dialog.show(activity.supportFragmentManager, "RoomExitDialog")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        // clear overlay when activity destroy
        RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_NO_OVERLAY, false)
    }
}
