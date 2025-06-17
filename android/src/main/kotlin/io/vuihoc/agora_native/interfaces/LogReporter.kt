package io.vuihoc.agora_native.interfaces

import android.content.Context

interface LogReporter {
    fun init(context: Context) {}

    fun setUserId(id: String) {}

    fun report(item: Map<String, String>)
}