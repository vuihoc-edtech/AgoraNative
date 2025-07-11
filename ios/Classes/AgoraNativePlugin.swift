import Flutter
import UIKit

var globalSessionId = UUID().uuidString

public class AgoraNativePlugin: NSObject, FlutterPlugin {
    static var pluginBundle: Bundle?
    static var env = Env()
    // Get the top-most view controller that can present
    static func topViewController() -> UIViewController? {
        var topController = UIApplication.shared.keyWindow?.rootViewController
        while let presentedController = topController?.presentedViewController {
            topController = presentedController
        }
        return topController
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        pluginBundle = Bundle(for: AgoraNativePlugin.self)
        let channel = FlutterMethodChannel(name: "agora_native", binaryMessenger: registrar.messenger())
        let instance = AgoraNativePlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "getPlatformVersion":
            result("iOS " + UIDevice.current.systemVersion)
        case "joinClassRoom":
            guard AuthStore.shared.user != nil, let args = call.arguments as? Dictionary<String, Any> else {
                result(-2)
                break
            }
            if #available(iOS 13.0, *) {
                if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene {
                    let windows = windowScene.windows
                    for window in windows {
                        Theme.shared.setupWindowTheme(window)
                    }
                }
            }
            let roomID = (args["roomID"] as? String) ?? ""
            let cam = (args["cam"] as? Bool) ?? false
            let mic = (args["mic"] as? Bool) ?? false
            ClassroomCoordinator.shared
                .enterClassroomFromFlutter(uuid: roomID, cam: cam, mic: mic,
                                                                  result: result)
        case "login":
            result("iOS " + UIDevice.current.systemVersion)
        case "saveLoginInfo":
            guard let arguments = call.arguments as? Dictionary<String, Any> else {
                result(false)
                break
            }
            saveLoginInfo(arguments)
            result(true)
        case "getGlobalUUID":
            result(globalSessionId)
        case "saveConfigs":
            guard let arguments = call.arguments as? Dictionary<String, Any> else {
                result(false)
                break
            }
            saveConfigs(arguments)
            result(true)
            break
        case "setBotUsers":
            guard let arguments = call.arguments as? [String] else {
                result(false)
                break
            }
            setBotUsers(users: arguments)
            result(true)
            break
            
        case "setWhiteBoardBackground":
            setWhiteboardBackground(call.arguments)
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    private func saveLoginInfo(_ argument: Dictionary<String, Any>) {
        AuthStore.shared.logout();
        let name = argument["name"] as? String ?? ""
        let avatar = argument["avatar"] as? String ?? ""
        let userUUID = argument["userUUID"] as? String ?? ""
        let token = argument["token"] as? String ?? ""
        let user = User(name: name, avatar: avatar, userUUID: userUUID, token: token, hasPhone: false, hasPassword: false)
        AuthStore.shared.processLoginSuccessUserInfo(user)
    }
    
    private func saveConfigs(_ argument: Dictionary<String, Any>) {
        let agora = argument["agora"] as? [String: Any]
        let agoraAppId = agora?["appId"] as? String ?? ""
        
        let cloud = argument["cloudStorage"] as? [String: Any]
        let accessKey = cloud?["accessKey"] as? String ?? ""
        
        let whiteboard = argument["whiteboard"] as? [String: Any]
        let whiteboardAppId = whiteboard?["appId"] as? String ?? ""
        
        let baseUrl = argument["baseUrl"] as? String ?? ""
        if !agoraAppId.isEmpty {
            AgoraNativePlugin.env.agoraAppId = agoraAppId
        }
        
        if !accessKey.isEmpty {
            AgoraNativePlugin.env.ossAccessKeyId = accessKey
        }
        
        if !whiteboardAppId.isEmpty {
            AgoraNativePlugin.env.netlessAppId = whiteboardAppId
        }
        
        if !baseUrl.isEmpty {
            if baseUrl.contains("https://") {
                AgoraNativePlugin.env.baseURL = baseUrl
            } else {
                AgoraNativePlugin.env.baseURL = "https://\(baseUrl)"
            }
        }
    }
    
    private func setBotUsers(users: [String]) {
        VHConfigsStore.shared.botUsers = users
    }
    
    private func setWhiteboardBackground(_ color: Any?) {
        guard let colorNS = color as? NSNumber else {
            return
        }
        
        let intValue = colorNS.uint32Value

        let red   = (intValue >> 16) & 0xFF
        let green = (intValue >> 8)  & 0xFF
        let blue  = intValue         & 0xFF

        let hexString = String(format: "#%02X%02X%02X", red, green, blue)
        VHConfigsStore.shared.whiteboardBackground = hexString
    }
    
    static let resourceBundle: Bundle = {
        let bundle = Bundle(for: AgoraNativePlugin.self)
        guard let resourceBundleURL = bundle.url(
            forResource: "agora_native", withExtension: "bundle")
        else { fatalError("agora_native.bundle not found!") }
        
        guard let resourceBundle = Bundle(url: resourceBundleURL)
        else { fatalError("Cannot access agora_native.bundle!") }
        
        return resourceBundle
    }()
}

func customLog(_ message: String) {
    let formatter = DateFormatter()
    formatter.dateFormat = "HH:mm:ss.SSS"
    let timestamp = formatter.string(from: Date())
    print("[\(timestamp)] \(message)")
}
