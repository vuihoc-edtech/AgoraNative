package io.agora.flat.ui.activity.playback

import android.os.Bundle
import android.view.WindowManager
import dagger.hilt.android.AndroidEntryPoint
import io.agora.vuihoc.agora_native.databinding.ActivityReplayBinding
import io.agora.flat.ui.activity.base.BaseActivity
import io.agora.flat.ui.activity.play.BaseComponent

@AndroidEntryPoint
class ReplayActivity : BaseActivity() {
    private lateinit var binding: ActivityReplayBinding
    private var componentSet: MutableSet<BaseComponent> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSystemUI()

        componentSet.add(ReplayPlayerComponent(this, binding.whiteboardContainer, binding.videoListContainer))
        componentSet.add(ReplayToolComponent(this, binding.toolsContainer))
        componentSet.forEach { lifecycle.addObserver(it) }
    }

    override fun onResume() {
        super.onResume()
        enableFullScreen()
    }

    private fun initSystemUI() {
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}