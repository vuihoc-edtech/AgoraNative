package io.agora.flat.common

import android.content.Context
import android.content.Intent
import io.agora.flat.Constants
import io.agora.flat.data.model.RoomPlayInfo
import io.agora.flat.ui.activity.play.ClassRoomActivity

object Navigator {
    fun launchRoomPlayActivity(context: Context, roomPlayInfo: RoomPlayInfo) {
        val intent = Intent(context, ClassRoomActivity::class.java).apply {
            putExtra(Constants.IntentKey.ROOM_UUID, roomPlayInfo.roomUUID)
            putExtra(Constants.IntentKey.ROOM_PLAY_INFO, roomPlayInfo)
        }
        context.startActivity(intent)
    }
}