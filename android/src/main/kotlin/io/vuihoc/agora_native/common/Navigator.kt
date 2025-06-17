package io.vuihoc.agora_native.common

import android.content.Context
import android.content.Intent
import io.vuihoc.agora_native.Constants
import io.vuihoc.agora_native.data.model.RoomPlayInfo
import io.vuihoc.agora_native.ui.activity.play.ClassRoomActivity

object Navigator {
    fun launchRoomPlayActivity(context: Context, roomPlayInfo: RoomPlayInfo) {
        val intent = Intent(context, ClassRoomActivity::class.java).apply {
            putExtra(Constants.IntentKey.ROOM_UUID, roomPlayInfo.roomUUID)
            putExtra(Constants.IntentKey.ROOM_PLAY_INFO, roomPlayInfo)
        }
        context.startActivity(intent)
    }

    fun launchRoomPlayActivity(
        context: Context,
        roomUUID: String,
        periodicUUID: String? = null,
        quickStart: Boolean = false,
        ) {
        val intent = Intent(context, ClassRoomActivity::class.java).apply {
            putExtra(Constants.IntentKey.ROOM_UUID, roomUUID)
            putExtra(Constants.IntentKey.PERIODIC_UUID, periodicUUID)
            putExtra(Constants.IntentKey.ROOM_QUICK_START, quickStart)
        }
        context.startActivity(intent)
    }
}