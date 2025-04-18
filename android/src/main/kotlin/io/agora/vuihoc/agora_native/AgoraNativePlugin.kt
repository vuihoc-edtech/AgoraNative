package io.agora.vuihoc.agora_native

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
// TODO: Import your AppKVCenter or equivalent class for storing user info
// import io.agora.flat.data.AppKVCenter
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.UUID

/** AgoraNativePlugin */
class AgoraNativePlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var applicationContext: Context? = null
    private lateinit var globalSessionId: String

    // Define constants for Intent action and extras
    companion object {
        const val ACTION_JOIN_CLASSROOM = "io.agora.flat.action.JOIN_CLASSROOM"
        const val EXTRA_ROOM_ID = "io.agora.flat.extra.ROOM_ID"
        const val TAG = "AgoraNativePlugin"
        // Assuming MainActivity is the entry point. Adjust if necessary.
        const val MAIN_ACTIVITY_CLASS_NAME = "io.agora.flat.ui.activity.MainActivity"
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "agora_native")
        channel.setMethodCallHandler(this)
        applicationContext = flutterPluginBinding.applicationContext
        globalSessionId = UUID.randomUUID().toString()
        Log.i(TAG, "Plugin attached. Global session ID: $globalSessionId")
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val context = applicationContext
        if (context == null && call.method != "getPlatformVersion") { // Allow getPlatformVersion even if context is somehow null briefly
            Log.e(TAG, "Context is null, cannot handle method call ${call.method}")
            result.error("CONTEXT_NULL", "Application context is null", null)
            return
        }

        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "joinClassRoom" -> {
                 if (context == null) { // Re-check context specifically for methods needing it
                    Log.e(TAG, "Context is null for joinClassRoom")
                    result.error("CONTEXT_NULL", "Application context is null for joinClassRoom", null)
                    return
                 }
                val roomId = call.arguments as? String
                if (roomId.isNullOrBlank()) {
                    Log.e(TAG, "Room ID is null or blank")
                    result.error("INVALID_ARGUMENT", "Room ID is required", null)
                    return
                }

                Log.i(TAG, "Attempting to join classroom with ID: $roomId")
                try {
                    val intent = Intent(context, Class.forName(MAIN_ACTIVITY_CLASS_NAME)).apply {
                        action = ACTION_JOIN_CLASSROOM
                        putExtra(EXTRA_ROOM_ID, roomId)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    context.startActivity(intent)
                    result.success(null)
                } catch (e: ClassNotFoundException) {
                     Log.e(TAG, "MainActivity class not found at $MAIN_ACTIVITY_CLASS_NAME", e)
                     result.error("ACTIVITY_NOT_FOUND", "Main activity class not found", e.message)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start activity for joining classroom", e)
                    result.error("ACTIVITY_START_FAILED", "Failed to start activity", e.message)
                }
            }
            "login" -> {
                // The purpose of this method call is unclear from the Swift code.
                // It just returns the OS version there. Returning notImplemented for now.
                Log.w(TAG, "'login' method called, but its Android implementation is not defined.")
                result.notImplemented()
            }
            "saveLoginInfo" -> {
                 if (context == null) { // Re-check context specifically for methods needing it
                    Log.e(TAG, "Context is null for saveLoginInfo")
                    result.error("CONTEXT_NULL", "Application context is null for saveLoginInfo", null)
                    return
                 }
                val arguments = call.arguments as? Map<String, Any>
                if (arguments == null) {
                    Log.e(TAG, "Arguments are null for saveLoginInfo")
                    result.error("INVALID_ARGUMENT", "Arguments are required for saveLoginInfo", null)
                    return
                }

                try {
                    val name = arguments["name"] as? String ?: ""
                    val avatar = arguments["avatar"] as? String ?: ""
                    val userUUID = arguments["userUUID"] as? String ?: ""
                    val token = arguments["token"] as? String ?: ""

                    Log.i(TAG, "Saving login info: userUUID=$userUUID")

                    // TODO: Replace with your actual logic to save user info and token
                    // Example using a hypothetical AppKVCenter (adjust as needed):
                    // val appKVCenter = AppKVCenter.getInstance(context) // Or get instance via DI/Service Locator
                    // appKVCenter.setUserInfo(name, avatar, userUUID)
                    // appKVCenter.setAuthToken(token)

                    // Placeholder: Log the action
                     Log.i(TAG, "Placeholder: User info saved (name=$name, avatar=$avatar, userUUID=$userUUID, token present=${token.isNotEmpty()})")


                    result.success(true)
                } catch (e: ClassCastException) {
                    Log.e(TAG, "Invalid argument type for saveLoginInfo", e)
                    result.error("INVALID_ARGUMENT", "Invalid argument type provided", e.message)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save login info", e)
                    result.error("SAVE_FAILED", "Failed to save login info", e.message)
                }
            }
             "getGlobalUUID" -> {
                Log.i(TAG, "Returning global session ID: $globalSessionId")
                result.success(globalSessionId)
             }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        applicationContext = null
         Log.i(TAG, "Plugin detached.")
    }
}
