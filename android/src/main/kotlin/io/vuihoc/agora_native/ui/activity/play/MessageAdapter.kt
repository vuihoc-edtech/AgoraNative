package io.vuihoc.agora_native.ui.activity.play

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
// import dagger.hilt.EntryPoint
// import dagger.hilt.InstallIn
// import dagger.hilt.android.EntryPointAccessors
// import dagger.hilt.android.components.ActivityComponent
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.common.rtm.Message
import io.vuihoc.agora_native.common.rtm.NoticeMessageBody
import io.vuihoc.agora_native.common.rtm.TextMessageBody
import io.vuihoc.agora_native.data.repository.RoomRepository
import io.vuihoc.agora_native.data.repository.UserRepository
import io.vuihoc.agora_native.ui.manager.UserQuery

/**
 * 消息列表适配器
 */
class MessageAdapter (
    context: Context,
    private val dataSet: MutableList<Message> = mutableListOf(),
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private var userQuery: UserQuery = UserQuery(RoomRepository.getInstance())
    private var userUUID: String = UserRepository.getInstance().getUserUUID()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflater.inflate(R.layout.item_room_message, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]
        when (val body = item.body) {
            is TextMessageBody -> {
                if (item.sender == userUUID) {
                    viewHolder.rightMessageLayout.isVisible = true
                    viewHolder.leftMessageLayout.isVisible = false
                    viewHolder.noticeMessageLayout.isVisible = false

                    viewHolder.rightMessage.text = body.message
                } else {
                    viewHolder.rightMessageLayout.isVisible = false
                    viewHolder.leftMessageLayout.isVisible = true
                    viewHolder.noticeMessageLayout.isVisible = false

                    viewHolder.leftMessage.text = body.message
                    viewHolder.leftName.text = item.username()
                }
            }
            is NoticeMessageBody -> {
                viewHolder.rightMessageLayout.isVisible = false
                viewHolder.leftMessageLayout.isVisible = false
                viewHolder.noticeMessageLayout.isVisible = true

                viewHolder.noticeMessage.run {
                    text = if (body.ban) {
                        context.getString(R.string.message_muted)
                    } else {
                        context.getString(R.string.message_unmuted)
                    }
                }
            }
        }
    }

    private fun Message.username(): String {
        return userQuery.queryUser(sender)?.name ?: ""
    }

    override fun getItemCount() = dataSet.size

    fun setMessages(it: List<Message>) {
        dataSet.clear()
        dataSet.addAll(it)
        notifyDataSetChanged()
    }

    fun addMessagesAtHead(msgs: List<Message>) {
        dataSet.addAll(0, msgs)
        notifyItemRangeInserted(0, msgs.size)
    }

    fun addMessagesAtTail(msgs: List<Message>) {
        dataSet.addAll(msgs)
        notifyItemRangeInserted(dataSet.size - msgs.size, msgs.size)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val leftMessageLayout: View = view.findViewById(R.id.leftMessageLayout)
        val leftName: TextView = view.findViewById(R.id.name)
        val leftMessage: TextView = view.findViewById(R.id.leftMessage)
        val rightMessageLayout: View = view.findViewById(R.id.rightMessageLayout)
        val rightMessage: TextView = view.findViewById(R.id.rightMessage)
        val noticeMessageLayout: View = view.findViewById(R.id.noticeMessageLayout)
        val noticeMessage: TextView = view.findViewById(R.id.noticeMessage)
    }
}
