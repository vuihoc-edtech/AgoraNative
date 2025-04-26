package io.agora.flat.ui.activity.setting

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

@Composable
fun ComposeWebView(
    modifier: Modifier = Modifier,
    url: String,
    onBack: (webView: WebView?) -> Unit,
    onProgressChange: (progress: Int) -> Unit = {},
    onTitleChange: (title: String) -> Unit = {},
    initSettings: (webSettings: WebSettings?) -> Unit = {},
    onReceivedError: (error: WebResourceError?) -> Unit = {},
) {
    val webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            onProgressChange(newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            onTitleChange(title)
        }
    }

    val webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            onProgressChange(-1)
        }

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            onProgressChange(100)
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (null == request.url) return false
            val showOverrideUrl = request.url.toString()
            try {
                if (!showOverrideUrl.startsWith("http://") && !showOverrideUrl.startsWith("https://")) {
                    Intent(Intent.ACTION_VIEW, Uri.parse(showOverrideUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        view.context?.applicationContext?.startActivity(this)
                    }
                    return true
                }
            } catch (e: Exception) {
                return true
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            onReceivedError(error)
        }
    }

    var webView by remember { mutableStateOf<WebView?>(null) }

    val scope = rememberCoroutineScope()
    BackHandler {
        scope.launch { onBack(webView) }
    }

    // Due to limited knowledge,information and time, more input is needed here to verify the correctness
    DisposableEffect(
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    this.webViewClient = webViewClient
                    this.webChromeClient = webChromeClient
                    initSettings(this.settings)
                    webView = this
                    loadUrl(url)
                }
            })
    ) {
        onDispose {
            webView?.run {
                removeAllViews()
                destroy()
                webView = null
            }
        }
    }
}