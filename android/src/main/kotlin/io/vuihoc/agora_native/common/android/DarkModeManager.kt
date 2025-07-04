package io.vuihoc.agora_native.common.android

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit


object DarkModeManager {
    enum class Mode(val type: String) {
        Auto("auto"),
        Light("light"),
        Dark("dark");

        companion object {
            fun of(mode: String?): Mode {
                return values().find { it.type == mode } ?: Auto
            }
        }
    }

    private const val KEY_DARK_MODE = "key_dark_mode"

    private var store: SharedPreferences? = null

    fun init(application: Application) {
        store = application.getSharedPreferences("flat_config", Context.MODE_PRIVATE)
        setDarkMode(Mode.Light)
    }

    fun current(): Mode {
        return Mode.Light
//        return Mode.of(store?.getString(KEY_DARK_MODE, Mode.Auto.type))
    }

    fun update(mode: Mode) {
        setDarkMode(mode)
        store?.edit(commit = true) {
            putString(KEY_DARK_MODE, mode.type)
        }
    }

    private fun setDarkMode(mode: Mode) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                Mode.Auto -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                Mode.Light -> AppCompatDelegate.MODE_NIGHT_NO
                Mode.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }
}