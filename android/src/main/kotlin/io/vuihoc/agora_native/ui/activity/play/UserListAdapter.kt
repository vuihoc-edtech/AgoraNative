package io.vuihoc.agora_native.ui.activity.play

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.data.model.RoomUser
import io.vuihoc.agora_native.ui.view.FlatDrawables
import io.vuihoc.agora_native.util.inflate
import io.vuihoc.agora_native.util.showToast


/**
 * 用户列表
 */
class UserListAdapter(
    private val viewModel: ClassRoomViewModel,
    private val dataSet: MutableList<RoomUser> = mutableListOf(),
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.inflate(R.layout.item_room_user_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]

        holder.username.text = item.name
        holder.userOffline.isVisible = !item.isJoined
        holder.avatar.load(item.avatarURL) {
            crossfade(true)
            placeholder(R.drawable.ic_class_room_user_avatar)
        }
        holder.onStageSwitch.isChecked = item.isOnStage
        holder.onStageSwitch.setOnTouchListener { v, event ->
            val targetChecked = !(v as SwitchCompat).isChecked
            if (event.action == MotionEvent.ACTION_UP) {
                if (targetChecked && !viewModel.isOnStageAllowable()) {
                    showOnStageSizeLimitedToast(holder)
                } else {
                    viewModel.updateOnStage(item.userUUID, targetChecked)
                }
            }
            true
        }
        holder.allowDrawSwitch.isChecked = item.allowDraw
        holder.allowDrawSwitch.setOnCheckedChangeListener { it, isChecked ->
            if (it.isPressed) {
                viewModel.updateAllowDraw(item.userUUID, isChecked)
            }
        }
        holder.cameraSwitch.isSelected = item.videoOpen
        holder.cameraSwitch.setOnClickListener {
            val target = !it.isSelected
            viewModel.enableVideo(target, item.userUUID)
        }
        holder.micSwitch.isSelected = item.audioOpen
        holder.micSwitch.setOnClickListener {
            val target = !it.isSelected
            viewModel.enableAudio(target, item.userUUID)
        }
        holder.inRaiseHandOwner.isVisible = item.isRaiseHand && viewModel.isOwner()
        holder.inRaiseHandOther.isVisible = item.isRaiseHand && !viewModel.isOwner()
        holder.noRaiseHand.isVisible = !item.isRaiseHand

        if (viewModel.isOwner()) {
            holder.agreeHandUp.setOnClickListener {
                if (viewModel.isOnStageAllowable()) {
                    viewModel.acceptRaiseHand(item.userUUID)
                } else {
                    showOnStageSizeLimitedToast(holder)
                }
            }
            holder.cancelHandUp.setOnClickListener {
                viewModel.cancelRaiseHand(item.userUUID)
            }
        }

        holder.cameraSwitch.isVisible = item.isOnStage
        holder.forbidCameraSwitch.isVisible = !item.isOnStage
        holder.micSwitch.isVisible = item.isOnStage
        holder.forbidMicSwitch.isVisible = !item.isOnStage

        if (viewModel.isOwner()) {
            holder.onStageSwitch.isEnabled = true
            holder.allowDrawSwitch.isEnabled = true
            holder.cameraSwitch.isEnabled = item.isOnStage
            holder.micSwitch.isEnabled = item.isOnStage
        } else {
            holder.onStageSwitch.isEnabled = item.isOnStage && viewModel.isSelf(item.userUUID)
            holder.allowDrawSwitch.isEnabled = item.allowDraw && viewModel.isSelf(item.userUUID)
            holder.cameraSwitch.isEnabled = item.isOnStage && viewModel.isSelf(item.userUUID)
            holder.micSwitch.isEnabled = item.isOnStage && viewModel.isSelf(item.userUUID)
        }
    }

    private fun showOnStageSizeLimitedToast(holder: ViewHolder) {
        holder.itemView.context.showToast(R.string.onstage_size_limited)
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].userUUID.hashCode().toLong()
    }

    override fun getItemCount() = dataSet.size

    fun setData(data: List<RoomUser>) {
        dataSet.clear()
        dataSet.addAll(data.distinctBy { it.userUUID })
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.avatar)
        val username: TextView = view.findViewById(R.id.username)
        val userOffline: TextView = view.findViewById(R.id.user_offline)
        val onStageSwitch: SwitchCompat = view.findViewById(R.id.switch_on_stage)
        val allowDrawSwitch: SwitchCompat = view.findViewById(R.id.switch_allow_draw)
        val cameraSwitch: ImageView = view.findViewById(R.id.switch_camera)
        val forbidCameraSwitch: View = view.findViewById(R.id.forbid_switch_camera)
        val micSwitch: ImageView = view.findViewById(R.id.switch_mic)
        val forbidMicSwitch: View = view.findViewById(R.id.forbid_switch_mic)
        val agreeHandUp: TextView = view.findViewById(R.id.agree_handup)
        val cancelHandUp: TextView = view.findViewById(R.id.cancel_handup)
        val inRaiseHandOwner: View = view.findViewById(R.id.in_raise_hand_owner)
        val inRaiseHandOther: View = view.findViewById(R.id.in_raise_hand_other)
        val noRaiseHand: View = view.findViewById(R.id.no_raise_hand)

        init {
            cameraSwitch.setImageDrawable(io.vuihoc.agora_native.ui.view.FlatDrawables.createCameraDrawable(view.context))
            micSwitch.setImageDrawable(io.vuihoc.agora_native.ui.view.FlatDrawables.createMicDrawable(view.context))
        }
    }
}
