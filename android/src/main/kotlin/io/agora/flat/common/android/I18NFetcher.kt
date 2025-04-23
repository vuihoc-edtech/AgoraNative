package io.agora.flat.common.android

import android.content.Context
import io.agora.vuihoc.agora_native.R

class I18NFetcher private constructor(context: Context) {

    private val resources = context.applicationContext.resources

    companion object {
        const val JOIN_ROOM_RECORD_PMI_TITLE = "join_room_record_pmi_title"

        private val map = mapOf(
            JOIN_ROOM_RECORD_PMI_TITLE to R.string.join_room_record_pmi_title
        )

        @Volatile
        private var INSTANCE: I18NFetcher? = null

        fun getInstance(context: Context): I18NFetcher {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: I18NFetcher(context).also { INSTANCE = it }
            }
        }
    }

    fun getString(key: String): String {
        val resId = map[key] ?: return ""
        return resources.getString(resId)
    }

    fun getString(key: String, vararg formatArgs: Any?): String {
        val resId = map[key] ?: return ""
        return resources.getString(resId, *formatArgs)
    }
}
