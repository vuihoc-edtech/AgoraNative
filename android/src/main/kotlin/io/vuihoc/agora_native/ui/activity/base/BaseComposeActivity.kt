package io.vuihoc.agora_native.ui.activity.base

import android.content.pm.ActivityInfo
import io.vuihoc.agora_native.util.isPhoneMode

open class BaseComposeActivity : BaseActivity() {
    override fun lockOrientation() {
        if (isPhoneMode()) {
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }
}