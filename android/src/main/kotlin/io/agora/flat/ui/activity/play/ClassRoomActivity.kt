package io.agora.flat.ui.activity.play

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import dagger.hilt.android.AndroidEntryPoint
import io.agora.vuihoc.agora_native.databinding.ActivityRoomPlayBinding
import io.agora.flat.ui.activity.base.BaseActivity

@AndroidEntryPoint
class ClassRoomActivity : BaseActivity() {
    private lateinit var binding: ActivityRoomPlayBinding
    private var componentSet: MutableSet<BaseComponent> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        componentSet.add(WhiteboardComponent(this, binding.whiteboardContainer))
        componentSet.add(
            RtcComponent(
                this,
                binding.videoListContainer,
                binding.fullVideoContainer,
                binding.shareScreenContainer,
                binding.userWindowsContainer
            )
        )
        componentSet.add(RtmComponent(this, binding.messageContainer))
        componentSet.add(ToolComponent(this, binding.toolContainer))
        componentSet.add(ClassCloudComponent(this, binding.cloudStorageContainer))
        componentSet.add(ExtComponent(this, binding.extensionContainer, binding.roomStateContainer))
        componentSet.forEach { lifecycle.addObserver(it) }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableFullScreen()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        componentSet.forEach { it.onConfigurationChanged(newConfig) }
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}