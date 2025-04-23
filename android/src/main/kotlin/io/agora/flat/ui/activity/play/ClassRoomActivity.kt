package io.agora.flat.ui.activity.play

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import io.agora.flat.Constants
import io.agora.flat.common.android.AndroidClipboardController
import io.agora.flat.common.android.I18NFetcher
import io.agora.flat.common.board.AgoraBoardRoom
import io.agora.flat.common.board.WhiteSyncedState
import io.agora.flat.common.rtc.AgoraRtc
import io.agora.flat.common.rtc.RtcVideoController
import io.agora.flat.common.rtm.AgoraRtm
import io.agora.flat.data.AppDatabase
import io.agora.flat.data.manager.JoinRoomRecordManager
import io.agora.flat.data.repository.CloudRecordRepository
import io.agora.flat.data.repository.MessageRepository
import io.agora.flat.data.repository.RoomRepository
import io.agora.flat.di.interfaces.RtmApi
import io.agora.flat.event.EventBus
// import dagger.hilt.android.AndroidEntryPoint
import io.agora.vuihoc.agora_native.databinding.ActivityRoomPlayBinding
import io.agora.flat.ui.activity.base.BaseActivity
import io.agora.flat.ui.manager.RecordManager
import io.agora.flat.ui.manager.RoomErrorManager
import io.agora.flat.ui.manager.UserManager
import io.agora.flat.ui.manager.UserQuery
import io.agora.flat.ui.manager.WindowsDragManager
import io.agora.flat.ui.viewmodel.ChatMessageManager


class ClassRoomActivity : BaseActivity() {
    private lateinit var binding: ActivityRoomPlayBinding
    private var componentSet: MutableSet<BaseComponent> = mutableSetOf()
    private lateinit var viewModel: ClassRoomViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        componentSet.add(WhiteboardComponent(this, binding.whiteboardContainer))
        componentSet.add(
            RtcComponent(
                this,
                binding.videoListContainer,
                binding.fullVideoContainer,
                binding.shareScreenContainer,
                binding.userWindowsContainer
            )
        )
        componentSet.add(RtmComponent(this, binding.messageContainer))
        componentSet.add(ToolComponent(this, binding.toolContainer))
        componentSet.add(ClassCloudComponent(this, binding.cloudStorageContainer))
        componentSet.add(ExtComponent(this, binding.extensionContainer, binding.roomStateContainer))
        componentSet.forEach { lifecycle.addObserver(it) }
        val joinRoomRecordManager = JoinRoomRecordManager.getInstance(context = this)
        val i18NFetcher = I18NFetcher.getInstance(this)
        val roomRepository = RoomRepository.getInstance(
            joinRoomRecordManager,
            i18NFetcher
        )
        val userManager = UserManager(UserQuery(roomRepository = roomRepository))
        val database = Room.databaseBuilder(this, AppDatabase::class.java, "flat-database").build()
        val recordManager = RecordManager(
            cloudRecordRepository = CloudRecordRepository.getInstance(),
            userManager = userManager,
            recordHistoryDao = database.recordHistoryDao(),
        )
        val messageManager = ChatMessageManager()
        val roomErrorManager = RoomErrorManager()
        val rtmApi = AgoraRtm(messageRepository = MessageRepository.getInstance())
        val rtcApi = AgoraRtc()
        val rtcVideoController = RtcVideoController(rtcApi)
        val boardRoom = AgoraBoardRoom()
        val syncedClassState = WhiteSyncedState()
        val eventBus = EventBus()
        val clipboard = AndroidClipboardController(this)
        val savedStateHandle = SavedStateHandle() //
        val roomUUID = intent.getStringExtra(Constants.IntentKey.ROOM_UUID)
        val periodicUUID = intent.getStringExtra(Constants.IntentKey.PERIODIC_UUID)
        val quickStart = intent.getBooleanExtra(Constants.IntentKey.ROOM_QUICK_START, false)

        savedStateHandle[Constants.IntentKey.ROOM_UUID] = roomUUID
        savedStateHandle[Constants.IntentKey.PERIODIC_UUID] = periodicUUID
        savedStateHandle[Constants.IntentKey.ROOM_QUICK_START] = quickStart
        // Use factory
        val factory = ClassRoomViewModelFactory(
            savedStateHandle,
            userManager,
            recordManager,
            messageManager,
            roomErrorManager,
            rtmApi,
            rtcApi,
            rtcVideoController,
            boardRoom,
            syncedClassState,
            eventBus,
            clipboard,
            roomRepository = roomRepository
        )

        viewModel = ViewModelProvider(this, factory)[ClassRoomViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableFullScreen()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        componentSet.forEach { it.onConfigurationChanged(newConfig) }
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}