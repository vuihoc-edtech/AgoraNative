import Flutter
import AgoraClassroomSDK_iOS
import AgoraProctorSDK
import UIKit
import AgoraUIBaseViews

public class AgoraNativePlugin: NSObject, FlutterPlugin {
    static var center: FcrAppCenter?
    static var proctor: AgoraProctor?
    public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "agora_native", binaryMessenger: registrar.messenger())
    
    let instance = AgoraNativePlugin()
        
    registrar.addMethodCallDelegate(instance, channel: channel)
    center = FcrAppCenter()
//    AgoraLoading.initProperties()
//    AgoraToast.initProperties()
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    case "joinClassRoom":
        if let arguments = call.arguments as? [String: Any],
              let roomId = arguments["roomId"] as? String,
              let userName = arguments["userName"] as? String,
            let role = arguments["role"] as? Int
            {
               result("Received data successfully")
            joinClassRoom(roomId: roomId, userName: userName, role: role)
               
           } else {
               result(FlutterError(code: "INVALID_ARGUMENTS", message: "Missing roomId or userName", details: nil))
           }
    default:
      result(FlutterMethodNotImplemented)
    }
  }
    
    private func joinClassRoom(roomId: String, userName: String, role: Int) {
      // Get the root view controller
        AgoraLoading.loading()
        let userRole = FcrAppUserRole(rawValue: role)
        AgoraNativePlugin.center?.room.getRoomInfo(roomId: roomId,
                                isQuickStart: true) { [weak self] object in
            AgoraLoading.hide()
            
            let userId = FcrAppRoomUserIdCreater().quickStart(userName: userName,
                                                              userRole: userRole ?? .student,
                                                              roomType: object.sceneType)
            
            let config = FcrAppJoinRoomPreCheckConfig(roomId: roomId,
                                                      userId: userId,
                                                      userName: userName,
                                                      userRole: userRole ?? .student,
                                                      isQuickStart: true)
            
            AgoraNativePlugin.center?.localStorage.writeData(userName,
                                                key: .nickname)
            
            self?.joinRoomPreCheck(config: config)
        } failure: { [weak self] error in
            AgoraLoading.hide()
            self?.showErrorToast(error)
        }
    }
    
    func joinRoomPreCheck(config: FcrAppJoinRoomPreCheckConfig) {
        AgoraLoading.loading()
        
        AgoraNativePlugin.center?.room.joinRoomPreCheck(config: config) { [weak self] object in
            guard let `self` = self else {
                return
            }
            
            let userId = config.userId
            let userName = object.roomDetail.userName
            let userRole = config.userRole
            
            let roomType = object.roomDetail.sceneType
            let roomName = object.roomDetail.roomName
            let roomId = object.roomDetail.roomId
            
            let appId = object.appId
            let token = object.token
            
            let region = AgoraNativePlugin.center?.urlGroup.region
            let streamLatency = object.roomDetail.roomProperties.latencyLevel
            
            // The language and mode displayed in the room are determined by
            // the global variables `agora_ui_language` and `agora_ui_mode`.
            agora_ui_language = AgoraNativePlugin.center?.language.proj()
            if let uiMode = AgoraNativePlugin.center?.uiMode.toAgoraType() {
                agora_ui_mode = uiMode
            }
            
            // Is the watermark displayed in the room
            let hasWatermark = object.roomDetail.roomProperties.watermark
            
            switch roomType {
            case .oneToOne, .smallClass, .lectureHall:
                let options = AgoraEduLaunchConfig(userName: userName,
                                                   userUuid: userId,
                                                   userRole: userRole.toClassroomType(),
                                                   roomName: roomName,
                                                   roomUuid: roomId,
                                                   roomType: roomType.toClassroomType(),
                                                   appId: appId,
                                                   token: token)
                
                options.mediaOptions.latencyLevel = streamLatency.toClassroomType()
                if let region = region {
                    options.region = region.toClassroomType()
                } else {
                    options.region = .AP
                }
                
                self.joinClassroom(config: options,
                                   hasWatermark: hasWatermark)
            case .proctor:
                let video = AgoraProctorVideoEncoderConfig()
                let media = AgoraProctorMediaOptions(videoEncoderConfig: video,
                                                     latencyLevel: streamLatency.toProctorType())
                var protocType: AgoraProctorRegion
                if let region = region {
                    protocType = region.toProctorType()
                } else {
                    protocType = .AP
                }
                let options = AgoraProctorLaunchConfig(userName: userName,
                                                       userUuid: userId,
                                                       userRole: userRole.toProctorType(),
                                                       roomName: roomName,
                                                       roomUuid: roomId,
                                                       appId: appId,
                                                       token: token,
                                                       region: protocType,
                                                       mediaOptions: media)
                
                self.joinProctorRoom(config: options)
            default:
                break
            }
        } failure: { [weak self] error in
            AgoraLoading.hide()
            self?.showErrorToast(error)
        }
    }

    func joinClassroom(config: AgoraEduLaunchConfig,
                       hasWatermark: Bool) {
        insertWidgetSampleToClassroom(config,
                                      hasWatermark: hasWatermark)

        AgoraClassroomSDK.setDelegate(self)
        
        AgoraClassroomSDK.launch(config) {
            AgoraLoading.hide()
        } failure: { [weak self] error in
            AgoraLoading.hide()
            self?.showErrorToast(error)
        }
    }

    func joinProctorRoom(config: AgoraProctorLaunchConfig) {
        let proctor = AgoraProctor(config: config)

        proctor.delegate = self
        
        proctor.launch {
            AgoraLoading.hide()
        } failure: { [weak self] error in
            AgoraLoading.hide()
            self?.showErrorToast(error)
        }
        
        AgoraNativePlugin.proctor = proctor
    }
    
    func insertWidgetSampleToClassroom(_ config: AgoraEduLaunchConfig,
                                       hasWatermark: Bool) {
        // Widget Doc CN: https://doc.shengwang.cn/doc/flexible-classroom/ios/advanced-features/widget
        // Widget Doc EN: https://docs.agora.io/en/flexible-classroom/develop/embed-custom-plugin?platform=ios
        // This provides an example of how to register a widget in the room.
        
        let sample = FcrAppWidgetSample()
        
        let link = AgoraNativePlugin.center?.urlGroup.invitation(roomId: config.roomUuid,
                                              inviterName: config.userName)
        guard let link = link else { return }
        config.widgets[sample.sharingLinkWidgetId] = sample.createSharingLink(link)
        
        if let cloudDrive = config.widgets[sample.cloudDriveId],
           config.userRole == .teacher {
            cloudDrive.extraInfo = sample.cloudDriveExCourseware()
        }
        
        if hasWatermark {
            config.widgets[sample.watermarkWidgetId] = sample.createWatermark(text: config.userName)
        }
    }
    
    func showErrorToast(_ error: Error) {
        var appError: FcrAppError
        
        if let errorObj = error as? FcrAppError {
            appError = errorObj
        } else {
            let nsError = error as NSError
            
            appError = FcrAppError(code: nsError.code,
                                   message: nsError.debugDescription)
        }
        
        var message: String
        
        switch appError.code {
        case 30403100: message = "fcr_user_tips_prohibited_join_room".localized()
        case -1:       message = "fcr_error_network_exception".localized()
        default:       message = appError.description()
        }
        
        showToast(message,
                  type: .error)
    }
    
    func showToast(_ message: String,
                   type: AgoraToastType = .notice) {
        AgoraToast.toast(message: message,
                         type: type)
    }
}

// MARK: - AgoraEduClassroomSDKDelegate
extension AgoraNativePlugin: AgoraEduClassroomSDKDelegate {
    public func classroomSDK(_ classroom: AgoraClassroomSDK,
                      didExit reason: AgoraEduExitReason) {
        AgoraLoading.initProperties()
    }
}

// MARK: - AgoraProctorDelegate
extension AgoraNativePlugin: AgoraProctorDelegate {
    public func onExit(reason: AgoraProctorExitReason) {
        AgoraNativePlugin.proctor = nil
        
        AgoraLoading.initProperties()
    }
}
