package io.agora.flat.ui.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.SavedStateHandle
import android.os.Bundle
import io.agora.flat.di.ViewModelFactory

/**
 * Thay thế cho flatViewModel(), sử dụng ViewModelFactory được tạo
 */
@Composable
inline fun <reified VM : ViewModel> flatViewModel(): VM {
    return viewModel(factory = ViewModelFactory.getInstance())
}

/**
 * Extension function cho Activity
 */
inline fun <reified T : ViewModel> ComponentActivity.flatViewModel(): T {
    return ViewModelProvider(this, ViewModelFactory.getInstance())[T::class.java]
}

/**
 * Extension function cho Fragment
 */
inline fun <reified T : ViewModel> Fragment.flatViewModel(
    owner: ViewModelStoreOwner = this
): T {
    return ViewModelProvider(owner, ViewModelFactory.getInstance())[T::class.java]
}

/**
 * Hàm tiện ích để chuyển đổi Bundle thành Map
 */
fun Bundle.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    keySet().forEach { key ->
        map[key] = get(key)
    }
    return map
}

/**
 * Extension function cho SavedStateHandle ViewModels trong Activity
 */
inline fun <reified T : ViewModel> ComponentActivity.createSavedStateViewModelFactory(
    defaultArgs: Bundle? = intent?.extras
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            if (modelClass.isAssignableFrom(T::class.java)) {
                // Mỗi ViewModel cần được xử lý riêng ở đây
                when (T::class.java.simpleName) {
                    "PreviewViewModel" -> {
                        val appEnv = io.agora.flat.di.GlobalInstanceProvider.getAppEnv()
                        val stateHandle = if (defaultArgs != null) {
                            SavedStateHandle(defaultArgs.toMap())
                        } else {
                            SavedStateHandle()
                        }
                        return io.agora.flat.ui.activity.cloud.preview.PreviewViewModel(
                            stateHandle,
                            appEnv
                        ) as VM
                    }
                    // Thêm các ViewModel khác ở đây khi cần thiết
                }
            }
            return ViewModelFactory.getInstance().create(modelClass)
        }
    }
} 