//
//  ClassRoomFactory.swift
//  Flat
//
//  Created by xuyunshi on 2021/11/8.
//  Copyright © 2021 agora.io. All rights reserved.
//

import AgoraRtcKit
import Fastboard
import Foundation
import RxCocoa
import Whiteboard

enum ClassroomFactory {
    static func getClassRoomViewController(withPlayInfo playInfo: RoomPlayInfo,
                                           basicInfo: RoomBasicInfo,
                                           deviceStatus: DeviceState) -> ClassRoomViewController
    {
        // Config Rtc
        let rtc = Rtc(appId: AgoraNativePlugin.env.agoraAppId,
                      channelId: playInfo.roomUUID,
                      token: playInfo.rtcToken,
                      uid: playInfo.rtcUID,
                      communication: basicInfo.roomType == .oneToOne,
                      isFrontMirror: ClassroomDefaultConfig.frontCameraMirror,
                      isUsingFront: ClassroomDefaultConfig.usingFrontCamera,
                      screenShareInfo: playInfo.rtcShareScreen,
                      localCameraOn: deviceStatus.camera,
                      localAudioOn: deviceStatus.mic
                        )
        startAudioCallSession()
        FastRoom.followSystemPencilBehavior = PerferrenceManager.shared.preferences[.applePencilFollowSystem] ?? true
        let fastRoomConfiguration: FastRoomConfiguration
        
        let region: Region = (FlatRegion(rawValue: basicInfo.region) ?? .CN_HZ).toFastRegion()
        let userName = AuthStore.shared.user?.name ?? ""

        let mixerDelegate: FastAudioMixerDelegate? = (PerferrenceManager.shared.preferences[.audioMixing] ?? false) ? rtc : nil
        print("set audio mixer \(mixerDelegate != nil)")
        fastRoomConfiguration = FastRoomConfiguration(appIdentifier: AgoraNativePlugin.env.netlessAppId,
                                                      roomUUID: playInfo.whiteboardRoomUUID,
                                                      roomToken: playInfo.whiteboardRoomToken,
                                                      region: region,
                                                      userUID: AuthStore.shared.user?.userUUID ?? "",
                                                      userPayload: .init(nickName: userName),
                                                      audioMixerDelegate: mixerDelegate)

        if var ua = fastRoomConfiguration.whiteSdkConfiguration.value(forKey: "netlessUA") as? [String] {
            let env = AgoraNativePlugin.env
            let str = "FLAT/NETLESS@\(env.version)"
            ua.append("fastboard/\(Fastboard.version())")
            ua.append(str)
            fastRoomConfiguration.whiteSdkConfiguration.setValue(ua, forKey: "netlessUA")
        }
        fastRoomConfiguration.whiteSdkConfiguration.enableSyncedStore = true
        fastRoomConfiguration.whiteSdkConfiguration.disableNewPencilStroke = !(PerferrenceManager.shared.preferences[.pencilTail] ?? true)
        let isOwner = basicInfo.isOwner
        fastRoomConfiguration.whiteRoomConfig.windowParams?.collectorStyles = ["top": "8px", "right": "8px"]
        fastRoomConfiguration.whiteRoomConfig.windowParams?.scrollVerticalOnly = true
        fastRoomConfiguration.whiteRoomConfig.windowParams?.stageStyle = "box-shadow: 0 0 0"
        fastRoomConfiguration.whiteRoomConfig.disableEraseImage = true
        fastRoomConfiguration.whiteRoomConfig.disableCameraTransform = !isOwner
        fastRoomConfiguration.whiteSdkConfiguration.log = true
        fastRoomConfiguration.whiteSdkConfiguration.loggerOptions = ["printLevelMask": WhiteSDKLoggerOptionLevelKey.debug.rawValue]
        fastRoomConfiguration.whiteRoomConfig.isWritable = basicInfo.roomType.whiteboardAlwaysWritable ? true : isOwner
//        if let customBundlePath = AgoraNativePlugin.resourceBundle.path(forResource: "whiteboard_rebuild", ofType: "bundle"),
//           let customBundle = Bundle(path: customBundlePath),
//           let indexPath = customBundle.path(forResource: "index", ofType: "html")
//        {
//            fastRoomConfiguration.customWhiteboardUrl = URL(fileURLWithPath: indexPath).absoluteString
//        }
        //TODO: This link is whiteboard_rebuild with apps
        
        fastRoomConfiguration.customWhiteboardUrl = "https://vuihoc-edtech.github.io/white_board_with_apps/"
        Fastboard.globalFastboardRatio = 1 / ClassRoomLayoutRatioConfig.whiteboardRatio
        let fastboardViewController = FastboardViewController(fastRoomConfiguration: fastRoomConfiguration)

        let camera = isOwner ? deviceStatus.camera : false
        let mic = isOwner ? deviceStatus.mic : false
        let initDeviceState = DeviceState(mic: mic, camera: camera)

        let agoraRtm = AgoraRtm(rtmToken: playInfo.rtmToken,
                           rtmUserUUID: playInfo.rtmUID,
                           agoraAppId: AgoraNativePlugin.env.agoraAppId)
        // Config Rtm
        let rtm: RtmProvider = agoraRtm
        let rtmChannel = agoraRtm.joinChannelId(playInfo.rtmChannelId)
            .asObservable()
            .share(replay: 1, scope: .forever)
            .asSingle()

        // Config State imp
        let syncedStore = ClassRoomSyncedStore()
        let videoLayoutStore = VideoLayoutStoreImp()
        fastboardViewController.bindStore = syncedStore
        fastboardViewController.bindLayoutStore = videoLayoutStore

//        let imp = ClassroomStateMock()
        let imp = ClassroomStateHandlerImp(syncedStore: syncedStore,
                                           rtmProvider: rtm,
                                           commandChannelRequest: rtmChannel,
                                           roomUUID: playInfo.roomUUID,
                                           ownerUUID: playInfo.ownerUUID,
                                           userUUID: AuthStore.shared.user?.userUUID ?? "",
                                           isOwner: playInfo.rtmUID == playInfo.ownerUUID,
                                           maxWritableUsersCount: basicInfo.roomType.maxWritableUsersCount,
                                           userInfo: RoomUserInfo(name: AuthStore.shared.user?.name ?? "", rtcUID: playInfo.rtcUID, avatarURL: AuthStore.shared.user?.avatar ?? ""),
                                           roomStartStatus: basicInfo.roomStatus,
                                           whiteboardBannedAction: fastboardViewController.isRoomBanned.filter { $0 }.asObservable().mapToVoid(),
                                           whiteboardRoomError: fastboardViewController.roomError.asObservable(),
                                           rtcError: rtc.errorPublisher.asObservable(),
                                           videoLayoutStore: videoLayoutStore)

        let isLocalUser: ((UInt) -> Bool) = { $0 == 0 || $0 == playInfo.rtcUID }
        let rtcViewModel = RtcViewModel(rtc: rtc,
                                        userRtcUid: playInfo.rtcUID,
                                        canUpdateLayout: basicInfo.isOwner,
                                        localUserValidation: isLocalUser,
                                        layoutStore: videoLayoutStore,
                                        userFetch: { rtcId -> RoomUser? in
                                            if rtcId == 0 { return imp.currentOnStageUsers[playInfo.rtmUID] }
                                            return imp.currentOnStageUsers.first(where: { $0.value.rtcUID == rtcId })?.value
                                        },
                                        userThumbnailStream: { rtcId -> AgoraVideoStreamType in
                                            guard let user = imp.currentOnStageUsers.first(where: { $0.value.rtcUID == rtcId })?.value else { return .low }
                                            let isTeacher = user.rtmUUID == playInfo.ownerUUID
                                            return playInfo.roomType.thumbnailStreamType(isUserTeacher: isTeacher)

                                        }, canUpdateDeviceState: { rtcUid in
                                            if isLocalUser(rtcUid) { return true }
                                            return basicInfo.isOwner
                                        }, canUpdateWhiteboard: { rtcUid in
                                            if basicInfo.isOwner {
                                                if isLocalUser(rtcUid) { return false }
                                                return true
                                            }
                                            return false
                                        }, canSendRewards: { rtcUid in
                                            if basicInfo.isOwner {
                                                if isLocalUser(rtcUid) { return false }
                                                return true
                                            }
                                            return false
                                        }, canResetLayout: { rtcUid in
                                            basicInfo.isOwner && isLocalUser(rtcUid)
                                        }, canMuteAll: { rtcUid in
                                            basicInfo.isOwner && isLocalUser(rtcUid)
                                        })
        let rtcViewController = RtcViewController(viewModel: rtcViewModel)
        rtcViewController.ownerUID = playInfo.ownerUUID
        let alertProvider = DefaultAlertProvider()
        let vm = ClassRoomViewModel(stateHandler: imp,
                                    initDeviceState: initDeviceState,
                                    isOwner: basicInfo.isOwner,
                                    userUUID: playInfo.rtmUID,
                                    roomUUID: playInfo.roomUUID,
                                    roomType: basicInfo.roomType,
                                    commandChannelRequest: rtmChannel,
                                    alertProvider: alertProvider,
                                    preferredDeviceState: deviceStatus,
                                    roomTimeLimit: 0)

        let userListViewController = ClassRoomUsersViewController(userUUID: playInfo.rtmUID, roomOwnerRtmUUID: playInfo.ownerUUID)
        let controller = ClassRoomViewController(viewModel: vm,
                                                 fastboardViewController: fastboardViewController,
                                                 rtcListViewController: rtcViewController,
                                                 userListViewController: userListViewController,
                                                 isOwner: basicInfo.isOwner,
                                                 ownerUUID: playInfo.ownerUUID,
                                                 beginTime: basicInfo.beginTime)
        alertProvider.root = controller
        print("joined classroom \(playInfo.roomUUID), \(String(describing: initDeviceState))")
        return controller
    }
    
    static func startAudioCallSession() {
        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.playAndRecord, mode: .voiceChat, options: [.allowBluetooth, .defaultToSpeaker])
        try? session.setActive(true)
    }
    
    static func cleanupAudioSession() {
        let session = AVAudioSession.sharedInstance()
        try? session.setActive(false)
        try? session.setCategory(.soloAmbient, mode: .default, options: [])
        try? session.setActive(true)
    }
}
