package io.vuihoc.agora_native.ui.activity.play

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import io.vuihoc.agora_native.common.board.AgoraBoardRoom
import io.vuihoc.agora_native.common.board.WhiteSyncedState
import io.vuihoc.agora_native.common.rtc.AgoraRtc
import io.vuihoc.agora_native.common.rtc.RtcVideoController
import io.vuihoc.agora_native.common.rtm.AgoraRtm
import io.vuihoc.agora_native.ui.activity.base.BaseActivity
//import io.vuihoc.agora_native.ui.manager.RecordManager
import io.vuihoc.agora_native.ui.manager.RoomErrorManager
import io.vuihoc.agora_native.ui.manager.UserManager
import io.vuihoc.agora_native.ui.manager.UserQuery
import io.vuihoc.agora_native.ui.viewmodel.ChatMessageManager
import io.vuihoc.agora_native.ui.viewmodel.MessageQuery
import io.vuihoc.agora_native.ui.viewmodel.MessageViewModel
import io.vuihoc.agora_native.ui.viewmodel.MessageViewModelFactory
import io.vuihoc.agora_native.databinding.ActivityRoomPlayBinding


class ClassRoomActivity : BaseActivity() {
    private lateinit var binding: ActivityRoomPlayBinding
    private var componentSet: MutableSet<BaseComponent> = mutableSetOf()
    private lateinit var classRoomViewModel: ClassRoomViewModel
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var classCloudViewModel: ClassCloudViewModel
    private val syncedClassState = WhiteSyncedState()
    private val boardRoom = AgoraBoardRoom(syncedClassState = syncedClassState)
    private val rtcVideoController = RtcVideoController(AgoraRtc.getInstance())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        componentSet.add(WhiteboardComponent(this, binding.whiteboardContainer, boardRoom))
        componentSet.add(
            RtcComponent(
                this,
                binding.videoListContainer,
                binding.fullVideoContainer,
                binding.shareScreenContainer,
                binding.userWindowsContainer,
                syncedClassState,
                rtcVideoController
            )
        )
        componentSet.add(RtmComponent(this, binding.messageContainer))
        componentSet.add(ToolComponent(this, binding.toolContainer, boardRoom = boardRoom))
        componentSet.add(ClassCloudComponent(this, binding.cloudStorageContainer))
        componentSet.add(ExtComponent(this, binding.extensionContainer, binding.roomStateContainer))
        componentSet.forEach { lifecycle.addObserver(it) }
        Log.d("ClassRoomActivity", "==============onCreate")
        createViewModels()
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

    private fun createViewModels() {
        val userQuery = UserQuery()
        val userManager = UserManager(userQuery)
        val messageManager = ChatMessageManager()
        val roomErrorManager = RoomErrorManager()

        val messageQuery = MessageQuery(
            rtmApi = AgoraRtm.getInstance(),
            userQuery = userQuery
        )
        val classRoomViewModelFactory = ClassRoomViewModelFactory(
            userManager = userManager,
//            recordManager = recordManager,
            messageManager = messageManager,
            roomErrorManager = roomErrorManager,
            rtcVideoController = rtcVideoController,
            boardRoom = boardRoom,
            syncedClassState = syncedClassState
        )

        val messageViewModelFactory = MessageViewModelFactory(
            userManager = userManager,
            syncedClassState = syncedClassState,
            messageManager = messageManager,
            messageQuery = messageQuery
        )

        val classCloudViewModelFactory = ClassCloudViewModelFactory(
            boardRoom, roomErrorManager
        )

        val extensionViewModelFactory = ExtensionViewModelFactory(
            roomErrorManager = roomErrorManager,
            boardRoom = boardRoom
        )

        classRoomViewModel = ViewModelProvider(this, classRoomViewModelFactory)[ClassRoomViewModel::class.java]
        messageViewModel = ViewModelProvider(this, messageViewModelFactory)[MessageViewModel::class.java]
        classCloudViewModel = ViewModelProvider(this, classCloudViewModelFactory)[ClassCloudViewModel::class.java]
        ViewModelProvider(this, extensionViewModelFactory)[ExtensionViewModel::class.java]
    }
}