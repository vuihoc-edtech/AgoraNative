package io.vuihoc.agora_native

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.herewhite.sdk.WhiteboardView
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.vuihoc.agora_native.common.FlatException
import io.vuihoc.agora_native.common.FlatNetException
import io.vuihoc.agora_native.common.Navigator
import io.vuihoc.agora_native.common.android.I18NFetcher
import io.vuihoc.agora_native.common.board.DeviceState
import io.vuihoc.agora_native.common.rtc.AgoraRtc
import io.vuihoc.agora_native.common.rtm.AgoraRtm
import io.vuihoc.agora_native.data.AppEnv
import io.vuihoc.agora_native.data.AppKVCenter
import io.vuihoc.agora_native.data.Failure
import io.vuihoc.agora_native.data.Success
import io.vuihoc.agora_native.data.model.UserInfo
import io.vuihoc.agora_native.data.repository.RoomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/** AgoraNativePlugin */
class AgoraNativePlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private var activity: Activity? = null
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "agora_native")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        AppKVCenter.getInstance().updateSessionId(UUID.randomUUID().toString())
        I18NFetcher.init(context)
        WhiteboardView.setEntryUrl("https://vuihoc-edtech.github.io/white_board_with_apps/")
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }

            "joinClassRoom" -> {
                val joinRoomInfo = call.arguments as? Map<String, Any>
                joinRoomInfo?.let {
                    val roomID = (it["roomID"] as? String) ?: ""
                    val cam = (it["cam"] as? Boolean) ?: false
                    val mic = (it["mic"] as? Boolean) ?: false
                    joinClassRoom(roomID, cam, mic, result)
                }

            }

            "saveLoginInfo" -> {
                Log.d(TAG, "saveLoginInfo")
                val user = call.arguments as Map<String, Any>
                val success = saveLoginInfo(user)
                result.success(success)
            }

            "getGlobalUUID" -> {
                result.success(AppKVCenter.getInstance().getSessionId())
            }

            "saveConfigs" -> {
                saveConfigs(call, result)
            }

            "setBotUsers" -> {
                val raw = call.arguments
                val users = if (raw is List<*> && raw.all { it is String }) {
                    raw.filterIsInstance<String>()
                } else {
                    emptyList()
                }
                AppKVCenter.getInstance().botUsersList.addAll(users)
            }

            "setWhiteBoardBackground" -> {
                val raw = call.arguments
                if (raw is Long) {
                    AppKVCenter.getInstance().whiteboardBackground = raw.toInt();
                }
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun saveConfigs(call: MethodCall, result: Result) {
        val config = call.arguments as? Map<*, *>
        Log.d(TAG, "saveConfigs ${config.toString()}")
        val agora = config?.get("agora") as? Map<*, *>
        val agoraId = agora?.get("appId") as? String
        val baseUrl = config?.get("baseUrl") as? String
        val cloudStorage = config?.get("cloudStorage") as? Map<*, *>
        val accessKey = cloudStorage?.get("accessKey") as? String
        val whiteboard = config?.get("whiteboard") as? Map<*, *>
        val whiteboardAppId = whiteboard?.get("appId") as? String
        AppEnv.getInstance().envItem.apply {
            if (agoraId != null) {
                agoraAppId = agoraId
            }
            if (baseUrl != null) {
                if (baseUrl.contains("https://")) {
                    serviceUrl = baseUrl
                } else {
                    serviceUrl = "https://$baseUrl"
                }

            }
            if (accessKey != null) {
                ossKey = accessKey
            }
            if (whiteboardAppId != null) {
                whiteAppId = whiteboardAppId
            }
        }

        postLogin()
        result.success(true)
    }

    private fun saveLoginInfo(user: Map<String, Any>): Boolean {
        AppKVCenter.getInstance().setToken(user["token"] as String)
        Log.d(TAG, user.toString())
        val userInfo = UserInfo(
            name = user["name"] as String,
            avatar = user["avatar"] as String,
            uuid = user["userUUID"] as String,
            hasPhone = user["hasPhone"] as? Boolean ?: false,
            hasPassword = user["hasPassword"] as? Boolean ?: false,
        )
        AppKVCenter.getInstance().setUserInfo(userInfo)
        return true
    }

    private fun joinClassRoom(uuid: String, cam: Boolean, mic: Boolean, result: Result) {
        if(uuid.isEmpty()) {
            result.success(-2)

        } else if (activity != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    when (val res = RoomRepository.getInstance().joinRoom(uuid)) {
                        is Success -> {
                            result.success(1)
                            AppKVCenter.getInstance()
                                .setDeviceStatePreference(DeviceState(camera = cam, mic = mic))
                            Navigator.launchRoomPlayActivity(activity!!, res.data)
                        }

                        is Failure -> {
                            if (res.exception is FlatNetException) {
                                result.success(res.exception.code)
                            } else {
                                result.success(-2)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    result.success(-2)
                }
            }
        } else {
            result.success(-2)
        }

    }

    private fun postLogin() {
        AgoraRtc.getInstance().init(context)
        AgoraRtm.getInstance().init(context)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    companion object {
        const val TAG = "VHLog AgoraNativePlugin"
    }
}