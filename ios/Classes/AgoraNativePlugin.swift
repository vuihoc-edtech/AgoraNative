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
            guard AuthStore.shared.user != nil, let roomId = call.arguments as? String else {
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
            ClassroomCoordinator.shared.enterClassroomFromFlutter(uuid: roomId,
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
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    private func saveLoginInfo(_ argument: Dictionary<String, Any>) {
        let name = argument["name"] as? String ?? ""
        let avatar = argument["avatar"] as? String ?? ""
        let userUUID = argument["userUUID"] as? String ?? ""
        let token = argument["token"] as? String ?? ""
        let user = User(name: name, avatar: avatar, userUUID: userUUID, token: token, hasPhone: false, hasPassword: false)
        AuthStore.shared.user = user
    }
    
    private static func initValues(_ channel: FlutterMethodChannel) {
        channel.invokeMethod("env", arguments: nil, result: { result in
            if let res = result as? Dictionary<String, String> {
                print("AgoraNativePlugin baseURL: \(res)")
                env.baseURL = res["baseUrl"] ?? ""
            }
        })
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
