package io.vuihoc.agora_native.ui.viewmodel

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import dagger.hilt.android.lifecycle.HiltViewModel
import io.vuihoc.agora_native.Constants
import io.vuihoc.agora_native.common.rtm.AgoraRtm
import io.vuihoc.agora_native.common.rtm.Message
import io.vuihoc.agora_native.common.rtm.MessageFactory
import io.vuihoc.agora_native.common.rtm.RoomBanEvent
import io.vuihoc.agora_native.data.repository.MiscRepository
import io.vuihoc.agora_native.data.repository.UserRepository
import io.vuihoc.agora_native.interfaces.RtmApi
import io.vuihoc.agora_native.interfaces.SyncedClassState
import io.vuihoc.agora_native.event.EventBus
import io.vuihoc.agora_native.event.MessagesAppended
import io.vuihoc.agora_native.ui.manager.UserManager
import io.vuihoc.agora_native.ui.util.ObservableLoadingCounter
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



class MessageViewModel(
    savedStateHandle: SavedStateHandle,
    private val messageManager: ChatMessageManager,
    private val syncedClassState: SyncedClassState,
    private val userManager: UserManager,
    private val messageQuery: MessageQuery,

    ) : ViewModel() {
    val roomUUID: String = savedStateHandle[Constants.IntentKey.ROOM_UUID]!!
    private val rtmApi: RtmApi = AgoraRtm.getInstance()
    private val eventbus: EventBus = EventBus.getInstance()
    private val _messageUpdate = MutableStateFlow(MessagesUpdate())
    val messageUpdate = _messageUpdate.asStateFlow()
    private val userRepository: UserRepository = UserRepository.getInstance()
    private val miscRepository: MiscRepository = MiscRepository.getInstance()
    private val messageLoading = ObservableLoadingCounter()

    val messageUiState = combine(
        syncedClassState.observeClassroomState(),
        messageLoading.observable,
        userManager.observeOwnerUUID(),
    ) { classState, loading, ownerUuid ->
        MessageUiState(
            ban = classState.ban,
            isOwner = ownerUuid == userRepository.getUserUUID(),
            loading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MessageUiState(),
    )

    init {
        initMessageQuery()
        loadHistoryMessage()
        viewModelScope.launch {
            eventbus.events.filterIsInstance<MessagesAppended>().collect {
                appendMessages(it.messages)
            }
        }
    }

    private fun initMessageQuery() {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 3600_000 * 24
        messageQuery.update(roomUUID, startTime, endTime, false)
    }

    fun loadHistoryMessage() {
        if (messageUiState.value.loading || !messageQuery.hasMore) {
            return
        }
        viewModelScope.launch(SupervisorJob()) {
            messageLoading.addLoader()
            if (messageManager.isEmpty()) {
                val msgs = messageQuery.loadMore().asReversed()
                appendMessages(msgs)
            } else {
                val msgs = messageQuery.loadMore().asReversed()
                prependMessages(msgs)
            }
            messageLoading.removeLoader()
        }
    }

    private fun appendMessages(msgs: List<Message>) {
        messageManager.appendMessages(msgs)
        _messageUpdate.value = _messageUpdate.value.copy(
            updateOp = MessagesUpdate.APPEND,
            messages = msgs,
        )
    }

    private fun prependMessages(msgs: List<Message>) {
        messageManager.prependMessages(msgs)

        _messageUpdate.value = _messageUpdate.value.copy(
            updateOp = MessagesUpdate.PREPEND,
            messages = msgs
        )
    }

    fun sendChatMessage(message: String) {
        viewModelScope.launch {
            if (miscRepository.censorRtm(message)) {
                rtmApi.sendChannelMessage(message)
                appendMessages(listOf(MessageFactory.createText(userRepository.getUserUUID(), message)))
            }
        }
    }

    fun muteChat(muted: Boolean) {
        viewModelScope.launch {
            if (userManager.isOwner()) {
                syncedClassState.updateBan(muted)
                rtmApi.sendChannelCommand(RoomBanEvent(roomUUID = roomUUID, status = muted))
                appendMessages(listOf(MessageFactory.createNotice(ban = muted)))
            }
        }
    }
}

data class MessagesUpdate(
    val updateOp: Int = IDLE,
    val messages: List<Message> = listOf(),
) {
    companion object {
        const val IDLE = 0
        const val APPEND = 1
        const val PREPEND = 2
    }
}

data class MessageUiState(
    val ban: Boolean = false,
    val isOwner: Boolean = false,
    val loading: Boolean = false,
)

class MessageViewModelFactory(
    private val messageManager: ChatMessageManager,
    private val syncedClassState: SyncedClassState,
    private val userManager: UserManager,
    private val messageQuery: MessageQuery,
) : AbstractSavedStateViewModelFactory() {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return MessageViewModel(
            savedStateHandle = handle,
            messageManager = messageManager,
            syncedClassState = syncedClassState,
            userManager = userManager,
            messageQuery = messageQuery
        ) as T
    }

}