package io.vuihoc.agora_native.ui.activity.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
// import dagger.hilt.android.lifecycle.HiltViewModel
import io.vuihoc.agora_native.common.board.AgoraBoardRoom
import io.vuihoc.agora_native.common.board.BoardError
import io.vuihoc.agora_native.common.board.BoardPhase
import io.vuihoc.agora_native.event.EventBus
import io.vuihoc.agora_native.event.RoomKickedEvent
import io.vuihoc.agora_native.ui.manager.RoomErrorManager
import io.vuihoc.agora_native.ui.util.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ExtensionViewModelFactory(
    private val boardRoom: AgoraBoardRoom,
    private val roomErrorManager: RoomErrorManager,
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExtensionViewModel(roomErrorManager, boardRoom) as T
    }
}

class ExtensionViewModel(
    private val errorManager: RoomErrorManager,
    private val boardRoom: AgoraBoardRoom,
) : ViewModel() {
    private val _state = MutableStateFlow(ExtensionState())
    val state = _state.asStateFlow()
    private val eventBus: EventBus = EventBus.getInstance()
    init {
        viewModelScope.launch {
            boardRoom.observeRoomPhase().collect { phase ->
                when (phase) {
                    BoardPhase.Connecting -> {
                        _state.value = _state.value.copy(loading = true)
                    }

                    BoardPhase.Connected -> {
                        _state.value = _state.value.copy(loading = false)
                    }

                    is BoardPhase.Error -> {
                        _state.value = _state.value.copy(error = UiMessage(phase.message))
                    }

                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            boardRoom.observeRoomError().collect { error ->
                when (error) {
                    is BoardError.Kicked -> {
                        eventBus.produceEvent(RoomKickedEvent)
                    }

                    is BoardError.Unknown -> {
                        _state.value = _state.value.copy(error = UiMessage(error.message))
                    }
                }
            }
        }

        viewModelScope.launch {
            errorManager.observeError().collect {
                _state.value = _state.value.copy(error = it)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

data class ExtensionState(
    val loading: Boolean = true,
    val error: UiMessage? = null,
)