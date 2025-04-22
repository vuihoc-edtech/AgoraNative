package io.agora.flat.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.SavedStateHandle
import io.agora.flat.ui.activity.home.HomeViewModel
import io.agora.flat.ui.activity.cloud.preview.PreviewViewModel
// Các import khác sẽ được thêm khi cần

class ViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                val roomRepository = GlobalInstanceProvider.get(io.agora.flat.data.repository.RoomRepository::class.java)
                val eventBus = GlobalInstanceProvider.getEventBus()
                val appKVCenter = GlobalInstanceProvider.getAppKvCenter()
                val networkObserver = GlobalInstanceProvider.get(io.agora.flat.di.interfaces.NetworkObserver::class.java)
                val logger = GlobalInstanceProvider.get(io.agora.flat.di.interfaces.Logger::class.java)
                HomeViewModel(roomRepository, eventBus, appKVCenter, networkObserver, logger) as T
            }
            
            // Commented out until we can properly handle SavedStateHandle
            // modelClass.isAssignableFrom(PreviewViewModel::class.java) -> {
            //     val appEnv = GlobalInstanceProvider.getAppEnv()
            //     PreviewViewModel(SavedStateHandle(), appEnv) as T
            // }
            
            // Các ViewModel khác sẽ được thêm vào sau
            
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
    
    companion object {
        private var instance: ViewModelFactory? = null
        
        fun getInstance(): ViewModelFactory {
            if (instance == null) {
                instance = ViewModelFactory()
            }
            return instance!!
        }
    }
} 