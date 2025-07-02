package io.vuihoc.agora_native.ui.activity.play

// import dagger.hilt.EntryPoint
// import dagger.hilt.InstallIn
// import dagger.hilt.android.EntryPointAccessors
// import dagger.hilt.android.components.ActivityComponent
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import io.vuihoc.agora_native.Constants
import io.vuihoc.agora_native.common.rtc.AgoraRtc
import io.vuihoc.agora_native.data.AppEnv
import io.vuihoc.agora_native.data.model.RoomStatus
import io.vuihoc.agora_native.interfaces.BoardRoom
import io.vuihoc.agora_native.interfaces.RtcApi
import io.vuihoc.agora_native.event.EventBus
import io.vuihoc.agora_native.event.ExpirationEvent
import io.vuihoc.agora_native.event.NotifyDeviceOffReceived
import io.vuihoc.agora_native.event.RequestDeviceReceived
import io.vuihoc.agora_native.event.RequestDeviceResponseReceived
import io.vuihoc.agora_native.event.RequestDeviceSent
import io.vuihoc.agora_native.event.RequestMuteAllSent
import io.vuihoc.agora_native.event.RoomsUpdated
import io.vuihoc.agora_native.event.TakePhotoEvent
import io.vuihoc.agora_native.ui.animator.SimpleAnimator
import io.vuihoc.agora_native.ui.manager.RoomOverlayManager
import io.vuihoc.agora_native.ui.view.AidienceExitDialog
import io.vuihoc.agora_native.ui.view.InviteDialog
import io.vuihoc.agora_native.ui.view.OwnerExitDialog
import io.vuihoc.agora_native.ui.view.RequestDeviceDialog
import io.vuihoc.agora_native.util.FlatFormatter
import io.vuihoc.agora_native.util.contentInfo
//import io.vuihoc.agora_native.util.isTabletMode
import io.vuihoc.agora_native.util.showToast
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.databinding.ComponentToolBinding
import io.vuihoc.agora_native.ui.view.PermissionDialog
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ToolComponent(
    activity: ClassRoomActivity,
    rootView: FrameLayout,
    private var boardRoom: BoardRoom
) : BaseComponent(activity, rootView) {

    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ComponentToolBinding
    private lateinit var toolAnimator: SimpleAnimator

    private val viewModel: ClassRoomViewModel by activity.viewModels()
    private lateinit var appEnv: AppEnv
    private lateinit var rtcApi: RtcApi
    private lateinit var eventBus: EventBus
    private lateinit var userListAdapter: UserListAdapter
//    private lateinit var acceptHandupAdapter: AcceptHandupAdapter

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        injectApi()
        initView()
        observeState()
//        registerForActivityResult()
    }

//    private fun registerForActivityResult() {
//        takePhotoLauncher =
//            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                rtcApi.enableLocalVideo(true)
//                if (result.resultCode == Activity.RESULT_OK) {
//                    val uri = result.data?.data ?: return@registerForActivityResult
//                    lifecycleScope.launch {
//                        val contentInfo = activity.contentInfo(uri) ?: return@launch
//                        eventBus.produceEvent(TakePhotoEvent(contentInfo))
//                    }
//                }
//            }
//    }

//    private fun launchTakePhoto() {
//        rtcApi.enableLocalVideo(false)
//        takePhotoLauncher.launch(Intent(activity, CameraActivity::class.java))
//    }

    private fun injectApi() {
        appEnv = AppEnv.getInstance()
        rtcApi = AgoraRtc.getInstance()
        eventBus = EventBus.getInstance()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                RoomOverlayManager.observeShowId().collect { areaId ->
                    if (areaId == RoomOverlayManager.AREA_ID_SETTING) {
                        showSettingLayout()
                    } else {
                        hideSettingLayout()
                    }
//                binding.cloudservice.isSelected = areaId == RoomOverlayManager.AREA_ID_CLOUD_STORAGE
//                    if (areaId == RoomOverlayManager.AREA_ID_USER_LIST) {
//                        showUserListLayout()
//                    } else {
//                        hideUserListLayout()
//                    }

//                    if (areaId == RoomOverlayManager.AREA_ID_ACCEPT_HANDUP) {
//                        showAcceptHandUpLayout()
//                    } else {
//                        hideAcceptHandUpLayout()
//                    }
                }
            }

        }

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.students.collect {
                    userListAdapter.setData(it)

                    binding.layoutUserList.studentSize.text = activity.getString(
                        R.string.user_list_student_size_format,
                        "${it.size}"
                    )
                    binding.layoutUserList.userList.isVisible = it.isNotEmpty()
                    binding.layoutUserList.listEmpty.isVisible = it.isEmpty()

//                val handupUsers = it.filter { user -> user.isRaiseHand }
//                acceptHandupAdapter.setData(handupUsers)

//                val handUpCount = handupUsers.size
//                binding.userlistDot.isVisible = handUpCount > 0
//                binding.handupCount.isVisible = handUpCount > 0
//                binding.handupCount.text = "$handUpCount"
//                binding.layoutAcceptHandup.listEmpty.isVisible = handUpCount == 0
//                binding.layoutAcceptHandup.handupListContainer.isVisible = handUpCount > 0
                }
            }

        }

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.teacher.collect {
                    binding.layoutUserList.teacherAvatar.load(it?.avatarURL) {
                        crossfade(true)
                        placeholder(R.drawable.ic_class_room_user_avatar)
                    }
                }
            }

        }

        lifecycleScope.launch {


            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.filterNotNull().collect {
                    binding.recordLayout.isVisible = it.isOwner
//                    binding.recordLayout.isVisible = it.isOwner && activity.isTabletMode()
//                binding.cloudservice.isVisible = it.allowDraw
//                binding.takePhoto.isVisible = it.allowDraw

//                binding.handupLayout.isVisible = !it.isOnStage && !it.ban
//                binding.handup.isSelected = it.isRaiseHand
//
//                binding.acceptHandupLayout.isVisible = it.isOwner
//                binding.handupCountLayout.isVisible = it.isOwner

                    binding.layoutSettings.switchVideo.isEnabled = it.isOnStage
                    binding.layoutSettings.switchAudio.isEnabled = it.isOnStage

                    binding.layoutSettings.switchVideo.isChecked = it.videoOpen
                    binding.layoutSettings.switchAudio.isChecked = it.audioOpen

                    binding.layoutUserList.teacherName.text =
                        activity.getString(R.string.user_list_teacher_name_format, it.ownerName)
                    binding.layoutUserList.stageOffAll.isVisible = it.isOwner
                    binding.layoutUserList.muteMicAll.isVisible = it.isOwner
                }
            }

        }

        lifecycleScope.launch {

            RoomOverlayManager.observeShowId().collect { areaId ->
                binding.message.isSelected = areaId == RoomOverlayManager.AREA_ID_MESSAGE
            }

        }

        lifecycleScope.launch {

            viewModel.messageCount.collect { count ->
                binding.messageDot.isVisible = count > 0 &&
                        RoomOverlayManager.getShowId() != RoomOverlayManager.AREA_ID_MESSAGE
            }

        }

        lifecycleScope.launch {


            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.classroomEvent.collect { event ->
                    when (event) {
                        is NotifyDeviceOffReceived -> {
                            if (event.camera == true) {
                                activity.showToast(R.string.teacher_turn_off_camera)
                            }
                            if (event.mic == true) {
                                activity.showToast(R.string.teacher_turn_off_mic)
                            }
                        }

                        is RequestDeviceResponseReceived -> {
                            if (event.camera == false) {
                                activity.showToast(
                                    activity.getString(
                                        R.string.refuse_turn_on_camera_format,
                                        event.username
                                    )
                                )
                            }
                            if (event.mic == false) {
                                activity.showToast(
                                    activity.getString(
                                        R.string.refuse_turn_on_mic_format,
                                        event.username
                                    )
                                )
                            }
                        }

                        is RequestDeviceSent -> {
                            activity.showToast(R.string.teacher_send_request_device)
                        }

                        is RequestDeviceReceived -> {
                            handleRequestDevice(event)
                        }

                        RequestMuteAllSent -> {
                            activity.showToast(R.string.toast_mute_all_mic)
                        }

                        is ExpirationEvent -> {
                            val expiration = FlatFormatter.timeHM(event.expireAt)
                            val minutes = event.leftMinutes
                            activity.showToast(
                                activity.getString(
                                    R.string.pay_room_about_to_end,
                                    expiration,
                                    minutes
                                )
                            )
                        }

                        else -> {}
                    }
                }
            }
        }


//         repeatOnLifecycle(Lifecycle.State.RESUMED) {
//            viewModel.recordState.collect { recordState ->
//                val isRecording = recordState != null
//                binding.startRecord.isVisible = !isRecording
//                binding.stopRecord.isVisible = isRecording
//            }
//        }
    }

    private fun handleRequestDevice(it: RequestDeviceReceived) {
        if (it.camera == true) {
            showRequestDeviceDialog(
                activity.getString(R.string.teacher_request_camera),
                onRefuse = viewModel::refuseCamera,
                onAgree = viewModel::agreeCamera
            )
        }
        if (it.mic == true) {
            showRequestDeviceDialog(
                activity.getString(R.string.teacher_request_mic),
                onRefuse = viewModel::refuseMic,
                onAgree = viewModel::agreeMic
            )
        }
    }

    private fun hideSettingLayout() {
        binding.layoutSettings.settingLayout.isVisible = false
        binding.setting.isSelected = false
    }

    private fun showSettingLayout() {
        binding.layoutSettings.settingLayout.isVisible = true
        binding.setting.isSelected = true
    }

    private fun hideUserListLayout() {
        binding.layoutUserList.root.isVisible = false
//        binding.userlist.isSelected = false
    }

    private val expectedUserListWidth =
        activity.resources.getDimensionPixelSize(R.dimen.room_class_user_list_width)
    private val panelMargin =
        activity.resources.getDimensionPixelSize(R.dimen.room_class_panel_margin_horizontal)

    private fun showUserListLayout() {
        binding.layoutUserList.root.isVisible = true
//        binding.userlist.isSelected = true

        // resize for small size devices
        val limitedWidth = binding.root.width - 2 * panelMargin
        if (expectedUserListWidth > limitedWidth) {
            val layoutParams = binding.layoutUserList.root.layoutParams
            layoutParams.width = limitedWidth
            binding.layoutUserList.root.layoutParams = layoutParams
        }
    }

    private fun showAcceptHandUpLayout() {
//        binding.layoutAcceptHandup.root.isVisible = true
//        binding.acceptHandup.isSelected = true
    }

    private fun hideAcceptHandUpLayout() {
//        binding.layoutAcceptHandup.root.isVisible = false
//        binding.acceptHandup.isSelected = false
    }

    private fun initView() {
        binding = ComponentToolBinding.inflate(activity.layoutInflater, rootView, true)

        val map: Map<View, (View) -> Unit> = mapOf(
            binding.message to {
                binding.messageDot.isVisible = false
                val shown = RoomOverlayManager.getShowId() != RoomOverlayManager.AREA_ID_MESSAGE
                RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_MESSAGE, shown)
            },
//            binding.cloudservice to {
//                val targetShow = RoomOverlayManager.getShowId() != RoomOverlayManager.AREA_ID_CLOUD_STORAGE
//                RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_CLOUD_STORAGE, targetShow)
//            },
//            binding.userlist to {
//                with(binding.layoutUserList.root) {
//                    if (isVisible) {
//                        hideUserListLayout()
//                    } else {
//                        showUserListLayout()
//                    }
//                    RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_USER_LIST, isVisible)
//                }
//            },
//            binding.invite to {
//                showInviteDialog()
//                binding.invite.isSelected = true
//            },
            binding.setting to {
                with(binding.layoutSettings.settingLayout) {
                    if (isVisible) {
                        hideSettingLayout()
                    } else {
                        showSettingLayout()
                    }
                    RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_SETTING, isVisible)
                }
            },
            binding.collapse to { toolAnimator.hide() },
            binding.expand to { toolAnimator.show() },
            binding.layoutSettings.exit to { handleExit() },

            binding.startRecord to {
                lifecycleScope.launch {
                    binding.startRecord.isEnabled = false
                    binding.startRecord.alpha = 0.2f
                    viewModel.startRecord()
                    activity.showToast(R.string.record_started_toast)
                    binding.startRecord.alpha = 1f
                    binding.startRecord.isEnabled = true
                }
            },
            binding.stopRecord to {
                lifecycleScope.launch {
                    binding.stopRecord.isEnabled = false
                    binding.stopRecord.alpha = 0.2f
                    viewModel.stopRecord()
                    activity.showToast(R.string.record_stopped_toast)
                    binding.stopRecord.alpha = 1f
                    binding.stopRecord.isEnabled = true
                }
            },
//            binding.handup to {
//                viewModel.raiseHand()
//            },
//            binding.acceptHandup to {
//                val target = !binding.layoutAcceptHandup.root.isVisible
//                if (target) {
//                    showAcceptHandUpLayout()
//                } else {
//                    hideAcceptHandUpLayout()
//                }
//                RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_ACCEPT_HANDUP, target)
//            },
//            binding.takePhoto to {
//                launchTakePhoto()
//            }
        )

        map.forEach { (view, action) -> view.setOnClickListener { action(it) } }

        toolAnimator = SimpleAnimator(
            onUpdate = ::onUpdateTool,
            onShowEnd = {
                binding.collapse.visibility = View.VISIBLE
                binding.expand.visibility = View.INVISIBLE
                resetToolsLayoutParams()
            },
            onHideEnd = {
                binding.collapse.visibility = View.INVISIBLE
                binding.expand.visibility = View.VISIBLE
            }
        )

        binding.layoutSettings.switchVideoArea.isChecked = viewModel.videoAreaShown.value
        binding.layoutSettings.switchVideoArea.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setVideoAreaShown(isChecked)
            hideSettingLayout()
        }

        binding.layoutSettings.switchVideo.setOnCheckedChangeListener { it, isChecked ->
            if (it.isPressed) {
                if (isChecked && !isGrantedPermission(Manifest.permission.CAMERA)) {
                    hideSettingLayout()
                    RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_SETTING, false)
                    val dialog = PermissionDialog("Máy ảnh", R.drawable.photo_library_24px)
                    dialog.show(activity.supportFragmentManager, "PermissionDialog")
                } else {
                    viewModel.enableVideo(isChecked)
                }
            }
        }

        binding.layoutSettings.switchAudio.setOnCheckedChangeListener { it, isChecked ->
            if (it.isPressed) {
                if (isChecked && !isGrantedPermission(Manifest.permission.RECORD_AUDIO)) {
                    hideSettingLayout()
                    RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_SETTING, false)
                    val dialog = PermissionDialog("Micro", R.drawable.mic_24px)
                    dialog.show(activity.supportFragmentManager, "PermissionDialog")
                } else {
                    viewModel.enableAudio(isChecked)
                }
            }
        }
        binding.layoutSettings.close.setOnClickListener {
            hideSettingLayout()
            RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_SETTING, false)
        }
        binding.layoutSettings.root.setOnClickListener {
            // block event
        }
//        binding.recordLayout.isVisible = activity.isTabletMode()

        userListAdapter = UserListAdapter(viewModel)
        binding.layoutUserList.userList.adapter = userListAdapter
        binding.layoutUserList.userList.layoutManager = LinearLayoutManager(activity)
        binding.layoutUserList.close.setOnClickListener {
            hideUserListLayout()
            RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_USER_LIST, false)
        }
        binding.layoutUserList.root.setOnClickListener {
            // block event
        }
        binding.layoutUserList.stageOffAll.setOnClickListener {
            viewModel.stageOffAll()
        }
        binding.layoutUserList.muteMicAll.setOnClickListener {
            viewModel.muteAllMic()
        }

//        acceptHandupAdapter = AcceptHandupAdapter(viewModel)
//        binding.layoutAcceptHandup.handupList.adapter = acceptHandupAdapter
//        binding.layoutAcceptHandup.handupList.layoutManager = LinearLayoutManager(activity)
    }

    private fun handleExit() {
        val state = viewModel.state.value ?: return
        if (state.isOwner && RoomStatus.Idle != state.roomStatus) {
            showOwnerExitDialog()
        } else {
            showAudienceExitDialog()
        }
    }

    private fun showOwnerExitDialog() {
        val dialog = OwnerExitDialog()
        dialog.setListener(object : OwnerExitDialog.Listener {
            override fun onClose() {

            }

            // 挂起房间
            override fun onLeftButtonClick() {
                updateRoomsAndFinish()
            }

            // 结束房间
            override fun onRightButtonClick() {
                lifecycleScope.launch {
                    if (viewModel.stopClass()) {
                        updateRoomsAndFinish()
                    } else {
                        activity.showToast(R.string.room_class_stop_class_fail)
                    }
                }
            }

            override fun onDismiss() {
                RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_OWNER_EXIT_DIALOG, false)
            }
        })
        dialog.show(activity.supportFragmentManager, "OwnerExitDialog")
        RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_OWNER_EXIT_DIALOG, true)

        activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }

    private fun updateRoomsAndFinish() {
        viewModel.sendGlobalEvent(RoomsUpdated)
        activity.finish()
    }

    private fun showAudienceExitDialog() {
        val dialog = AidienceExitDialog()
        dialog.setListener(object : AidienceExitDialog.Listener {
            override fun onClose() {
            }

            // 挂起房间
            override fun onLeftButtonClick() {
//                updateRoomsAndFinish()
            }

            // 结束房间
            override fun onRightButtonClick() {
                updateRoomsAndFinish()
            }

            override fun onDismiss() {
                RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_AUDIENCE_EXIT_DIALOG, false)
            }
        })
        RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_AUDIENCE_EXIT_DIALOG, true)
        dialog.show(activity.supportFragmentManager, "AidienceExitDialog")
    }

    private fun showRequestDeviceDialog(
        message: String,
        onAgree: () -> Unit,
        onRefuse: () -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        val prev = activity.supportFragmentManager.findFragmentByTag("RequestDeviceDialog")
        if (prev is DialogFragment) {
            prev.dismiss()
        }

        val dialog = RequestDeviceDialog().apply {
            arguments = Bundle().apply {
                putString(Constants.IntentKey.MESSAGE, message)
            }
        }
        dialog.setListener(object : RequestDeviceDialog.Listener {
            override fun onRefuse() {
                onRefuse()
            }

            override fun onAgree() {
                onAgree()
            }
        })
        dialog.show(activity.supportFragmentManager, "RequestDeviceDialog")
    }

    private fun showInviteDialog() {
        val inviteInfo = viewModel.getInviteInfo() ?: return

        val inviteLink = inviteInfo.link
        val datetime = FlatFormatter.dateWithDuring(inviteInfo.beginTime, inviteInfo.endTime)
        val roomTitle = inviteInfo.roomTitle
        val roomUuid = inviteInfo.roomUuid

        val inviteText = activity.getString(
            if (inviteInfo.isPmi) R.string.invite_pmi_text_format else R.string.invite_text_format,
            inviteInfo.username,
            roomTitle,
            roomUuid,
            datetime,
            inviteLink
        )

        val inviteTitle = activity.getString(
            if (inviteInfo.isPmi) R.string.invite_pmi_title_format else R.string.invite_title_format,
            inviteInfo.username
        )

        val dialog = InviteDialog().apply {
            arguments = Bundle().apply {
                putString(InviteDialog.INVITE_TITLE, inviteTitle)
                putString(InviteDialog.ROOM_NUMBER, roomUuid)
                putString(InviteDialog.ROOM_TITLE, roomTitle)
                putString(InviteDialog.ROOM_TIME, datetime)
            }
        }
        dialog.setListener(object : InviteDialog.Listener {
            override fun onCopy() {
                viewModel.setClipboard(inviteText)
                activity.showToast(R.string.copy_success)
            }

            override fun onHide() {
//                binding.invite.isSelected = false
                RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_INVITE_DIALOG, false)
            }
        })
        dialog.show(activity.supportFragmentManager, "InviteDialog")
        RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_INVITE_DIALOG, true)
    }

    private val itemSize =
        activity.resources.getDimensionPixelSize(R.dimen.room_class_button_area_size)

    private val collapseHeight = itemSize
    private val expandHeight: Int
        get() {
            val visibleCount = binding.extTools.children.count { it.isVisible }
            return itemSize * visibleCount
        }

    private fun onUpdateTool(value: Float) {
        val layoutParams = binding.extTools.layoutParams
        layoutParams.height = collapseHeight + (value * (expandHeight - collapseHeight)).toInt()
        binding.extTools.layoutParams = layoutParams
    }

    // TODO free layoutParams height for visible change of items
    private fun resetToolsLayoutParams() {
        val layoutParams = binding.extTools.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.extTools.layoutParams = layoutParams
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleExit()
        }
    }

    private fun isGrantedPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
