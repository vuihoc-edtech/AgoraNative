package io.vuihoc.agora_native

import android.app.Activity
import android.content.Context
import android.util.Log
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
import io.vuihoc.agora_native.common.Navigator
import io.vuihoc.agora_native.common.android.I18NFetcher
import io.vuihoc.agora_native.common.board.DeviceState
import io.vuihoc.agora_native.common.rtc.AgoraRtc
import io.vuihoc.agora_native.common.rtm.AgoraRtm
import io.vuihoc.agora_native.data.AppEnv
import io.vuihoc.agora_native.data.AppKVCenter
import io.vuihoc.agora_native.data.model.UserInfo
import java.util.UUID

/** AgoraNativePlugin */
class AgoraNativePlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel : MethodChannel
    private lateinit var context: Context
    private var activity: Activity? = null
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "agora_native")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        AppKVCenter.getInstance().updateSessionId(UUID.randomUUID().toString())
        I18NFetcher.init(context)
//        WhiteboardView.setEntryUrl("https://vuihoc-edtech.github.io/white_board_with_apps/")

    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "joinClassRoom" -> {
                val roomUUID = call.arguments as String
                joinClassRoom(roomUUID, result)
            }
            "saveLoginInfo" -> {
                Log.d("AgoraNativePlugin","saveLoginInfo")
                val user = call.arguments as Map<String, Any>
                val success = saveLoginInfo(user)
                result.success(success)
            }
            "getGlobalUUID" -> {
                result.success(AppKVCenter.getInstance().getSessionId())
            }
            "saveConfigs" -> {

                val config = call.arguments as? Map<*, *>
                Log.d("AgoraNativePlugin","saveConfigs ${config.toString()}")
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
                    if(whiteboardAppId != null) {
                        whiteAppId = whiteboardAppId
                    }
                }
                postLogin()
                result.success(true)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun saveLoginInfo(user: Map<String, Any>) : Boolean {
        AppKVCenter.getInstance().setToken(user["token"] as String)
        Log.d("AgoraNativePlugin", user.toString())
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

    private fun joinClassRoom(uuid: String, result: Result) {
            if(activity != null) {
                AppKVCenter.getInstance().setDeviceStatePreference(DeviceState(camera = true, mic = true))
                Navigator.launchRoomPlayActivity(activity!!, uuid, null, false)
                result.success(true)
//                CoroutineScope(Dispatchers.Main).launch {
//                    try {
//                        when (val res = RoomRepository.getInstance().joinRoom(uuid)) {
//                            is Success -> {
//                                result.success(true)
//                                AppKVCenter.getInstance().setDeviceStatePreference(DeviceState(camera = true, mic = true))
//                                Navigator.launchRoomPlayActivity(activity!!, res.data)
//                            }
//
//                            is Failure -> {
//                                result.success(false)
//                                Toast.makeText(context, "join room error", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        // Handle unexpected error
//                    }
//                }
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
}