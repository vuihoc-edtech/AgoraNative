package io.vuihoc.agora_native.ui.activity.play

import android.util.Log
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
// import dagger.hilt.EntryPoint
// import dagger.hilt.InstallIn
// import dagger.hilt.android.EntryPointAccessors
// import dagger.hilt.android.components.ActivityComponent
import io.vuihoc.agora_native.common.FlatException
import io.vuihoc.agora_native.common.rtm.AgoraRtm
import io.vuihoc.agora_native.databinding.ComponentMessageBinding
import io.vuihoc.agora_native.interfaces.RtmApi
import io.vuihoc.agora_native.ui.manager.RoomOverlayManager
import io.vuihoc.agora_native.ui.view.MessageListView
import io.vuihoc.agora_native.ui.viewmodel.MessageViewModel
import io.vuihoc.agora_native.ui.viewmodel.MessagesUpdate
import io.vuihoc.agora_native.util.KeyboardHeightProvider
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RtmComponent(
    activity: ClassRoomActivity,
    rootView: FrameLayout,
) : BaseComponent(activity, rootView) {

    private val messageViewModel: MessageViewModel by activity.viewModels()
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private lateinit var rtmApi: RtmApi
    private lateinit var binding: ComponentMessageBinding

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        injectApi()
        initView()
        observeData()
    }

    private fun injectApi() {
        rtmApi = AgoraRtm.getInstance()
    }

    private fun initView() {
        binding = ComponentMessageBinding.inflate(activity.layoutInflater, rootView, true)
        binding.root.isVisible = false

        binding.messageLv.setListener(object : MessageListView.Listener {
            override fun onSendMessage(msg: String) {
                messageViewModel.sendChatMessage(msg)
            }

            override fun onMute(muted: Boolean) {
                messageViewModel.muteChat(muted)
            }

            override fun onLoadMore() {
                messageViewModel.loadHistoryMessage()
            }
        })


        keyboardHeightProvider = KeyboardHeightProvider(activity)
            .setHeightListener(object : KeyboardHeightProvider.HeightListener {
                private var originBottomMargin: Int? = null
                override fun onHeightChanged(height: Int) {
                    if (originBottomMargin == null && binding.messageLv.isVisible) {
                        originBottomMargin =
                            (binding.messageLv.layoutParams as ConstraintLayout.LayoutParams).bottomMargin
                    }
                    if (originBottomMargin != null) {
                        val lp = binding.messageLv.layoutParams as ConstraintLayout.LayoutParams
                        lp.bottomMargin = height + originBottomMargin!!
                        binding.messageLv.postDelayed({
                            binding.messageLv.layoutParams = lp
                        }, 100)
                    }
                }
            })

        lateStartKeyboardHeightProvider()
    }

    private fun lateStartKeyboardHeightProvider() {
        val onWindowFocusChangeListener: ViewTreeObserver.OnWindowFocusChangeListener =
            ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) {
                keyboardHeightProvider?.start()
            } else {
                keyboardHeightProvider?.stop()
            }
        }
        binding.root.viewTreeObserver.addOnWindowFocusChangeListener(onWindowFocusChangeListener)
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                messageViewModel.messageUiState.filterNotNull().collect {
                    binding.messageLv.showBanBtn(it.isOwner)
                    binding.messageLv.setBan(it.ban, it.isOwner)
                    binding.messageLv.showLoading(it.loading)
                }
            }
        }

        lifecycleScope.launch {


            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                messageViewModel.messageUpdate.collect {
                    when (it.updateOp) {
                        MessagesUpdate.APPEND -> binding.messageLv.addMessagesAtTail(it.messages)
                        MessagesUpdate.PREPEND -> binding.messageLv.addMessagesAtHead(it.messages)
                    }
                }
            }

        }
        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                RoomOverlayManager.observeShowId().collect { areaId ->
                    binding.root.isVisible = areaId == RoomOverlayManager.AREA_ID_MESSAGE
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        keyboardHeightProvider?.dismiss()
        Log.d("Vuihoc_Log" ,"Rtm Destroy")
        runBlocking {
            try { rtmApi.logout() } catch (e: FlatException) { }
        }
    }
}