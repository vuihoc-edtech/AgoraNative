package io.agora.vuihoc.agora_native

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.content.Context
import com.google.gson.Gson
import io.agora.flat.common.Navigator
import io.agora.flat.data.AppKVCenter
import io.agora.flat.data.model.UserInfo
import java.util.UUID

/** AgoraNativePlugin */
class AgoraNativePlugin: FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel : MethodChannel
    private lateinit var context: Context

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "agora_native")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        AppKVCenter.getInstance().initStore(context)
        AppKVCenter.getInstance().updateSessionId(UUID.randomUUID().toString())
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "joinClassRoom") {
            val roomUUID = call.arguments as String
            joinClassRoom(roomUUID)
            result.success(null)
        } else if (call.method == "saveLoginInfo") {
            val user = call.arguments as Map<String, Any>
            val success = saveLoginInfo(user)
            result.success(success)
        } else if(call.method == "getGlobalUUID") {
            result.success(AppKVCenter.getInstance().getSessionId())
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
    
    // Phương thức để mở LoginActivity
    private fun saveLoginInfo(user: Map<String, Any>) : Boolean {
        val gson = Gson()
        AppKVCenter.getInstance().setToken(user["token"] as String)
        val userJson = gson.toJson(user)
        val userInfo = gson.fromJson(userJson, UserInfo::class.java)
        AppKVCenter.getInstance().setUserInfo(userInfo)
        return true
    }

    private fun joinClassRoom(uuid: String) {
        try {
            // Lấy class từ tên đầy đủ
            Navigator.launchRoomPlayActivity(
                context,
                uuid,
                null,
                true
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}