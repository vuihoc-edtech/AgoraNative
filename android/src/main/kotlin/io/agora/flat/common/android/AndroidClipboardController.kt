package io.agora.flat.common.android

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// TODO try replace by LocalClipboardManager.current
@Composable
fun rememberAndroidClipboardController(
    context: Context = LocalContext.current,
): AndroidClipboardController = remember(context) {
    AndroidClipboardController(context)
}

class AndroidClipboardController(context: Context) : ClipboardController {
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    override fun putText(text: CharSequence) {
        val clip: ClipData = ClipData.newPlainText("Flat", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun getText(): CharSequence {
        if (!clipboard.hasPrimaryClip())
            return ""
        return clipboard.primaryClip?.getItemAt(0)?.text ?: ""
    }
    companion object {
        @Volatile
        private var INSTANCE: AndroidClipboardController? = null

        fun init(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = AndroidClipboardController(context)
                    }
                }
            }
        }

        fun getInstance(): AndroidClipboardController {
            return INSTANCE ?: throw IllegalStateException("AndroidClipboardController is not initialized. Call init(context) first.")
        }
    }
}

interface ClipboardController {
    fun putText(text: CharSequence)

    fun getText(): CharSequence
}