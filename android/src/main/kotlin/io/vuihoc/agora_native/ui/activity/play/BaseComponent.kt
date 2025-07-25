package io.vuihoc.agora_native.ui.activity.play

import android.content.res.Configuration
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

abstract class BaseComponent(
    val activity: AppCompatActivity,
    val rootView: FrameLayout,
) : LifecycleOwner, DefaultLifecycleObserver {

    override val lifecycle: Lifecycle = activity.lifecycle

    open fun onConfigurationChanged(newConfig: Configuration) {}
}