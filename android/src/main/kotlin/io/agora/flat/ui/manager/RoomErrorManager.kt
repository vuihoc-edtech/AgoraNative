package io.agora.flat.ui.manager

// import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.agora.flat.ui.util.UiMessage
import kotlinx.coroutines.flow.*



class RoomErrorManager {
    private var error = MutableStateFlow<UiMessage?>(null)

    fun observeError(): Flow<UiMessage> = error.asStateFlow().filterNotNull().distinctUntilChanged()

    fun notifyError(text: String, exception: Throwable) {
        error.value = UiMessage(text, exception)
    }
}
