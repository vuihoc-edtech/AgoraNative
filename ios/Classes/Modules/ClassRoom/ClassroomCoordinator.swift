//
//  ClassroomCoordinator.swift
//  Flat
//
//  Created by xuyunshi on 2023/1/10.
//  Copyright © 2023 agora.io. All rights reserved.
//

import Foundation
import RxSwift
import Flutter

struct JoinRoomHistoryItem: Codable {
    let roomName: String
    let roomInviteId: String
}

class ClassroomCoordinator: NSObject {
    override private init() {
        super.init()
    }
    
    static let shared = ClassroomCoordinator()
    
    var currentClassroomUUID: String?
    var enterClassDate: Date?
    
    @objc func onClassroomLeaving() {
        // Just remove all the info for can't get the leaving scene identifier.
        currentClassroomUUID = nil
        if let enterClassDate {
            self.enterClassDate = nil
        }
    }
    
    private func fetchClassroomViewController(uuid: String) -> Single<ClassRoomViewController>
    {
        guard let user = AuthStore.shared.user else { return .error("user not login") }
        let deviceStatusStore = UserDevicePreferredStatusStore(userUUID: user.userUUID)
        let micOn = deviceStatusStore.getDevicePreferredStatus(.mic)
        let cameraOn = deviceStatusStore.getDevicePreferredStatus(.camera)
        let deviceState = DeviceState(mic: true, camera: true)
        return RoomPlayInfo.fetchByJoinWith(uuid: uuid)
            .concatMap { p -> Observable<(RoomPlayInfo, RoomBasicInfo)> in
                return RoomBasicInfo.fetchInfoBy(uuid: p.roomUUID, periodicUUID: nil)
                    .map { (p, $0) }
            }
            .map { ClassroomFactory.getClassRoomViewController(withPlayInfo: $0.0, basicInfo: $0.1, deviceStatus: deviceState) }
            .asSingle()
    }
    
    
    func enterClassroomFromFlutter(uuid: String,
                                   result: @escaping FlutterResult)
    {
        currentClassroomUUID = uuid
        enterClassDate = Date()
        
        fetchClassroomViewController(uuid: uuid)
            .observe(on: MainScheduler.instance)
            .subscribe(with: self, onSuccess: { _, vc in
                result(true)
                guard let main = AgoraNativePlugin.topViewController() else { return }
                if let _ = main.presentedViewController {
                    main.showActivityIndicator()
                    main.dismiss(animated: true) {
                        main.stopActivityIndicator()
                        main.present(vc, animated: true)
                    }
                } else {
                    main.present(vc, animated: true)
                }
            }, onFailure: { weakSelf, error in
                result(false)
                weakSelf.currentClassroomUUID = nil
                let controller = AgoraNativePlugin.topViewController()
                if let flatError = error as? FlatApiError {
                    if flatError == .RoomNotBegin {
                        let errorStr = String(format: NSLocalizedString("RoomNotBegin %d", bundle: AgoraNativePlugin.resourceBundle, comment: "room not begin alert"), Int(AgoraNativePlugin.env.joinEarly / 60))
                        controller?.showAlertWith(message: errorStr)
                    } else if flatError == .RoomNotBeginAndAddList {
                        let errorStr = String(format: NSLocalizedString("RoomNotBeginAndAddList %d", bundle: AgoraNativePlugin.resourceBundle, comment: "room not begin alert"), Int(AgoraNativePlugin.env.joinEarly / 60))
                        controller?.showAlertWith(message: errorStr)
                    } else {
                        controller?.showAlertWith(message: error.localizedDescription)
                    }
                } else {
                    controller?.showAlertWith(message: error.localizedDescription)
                }
            })
            .disposed(by: rx.disposeBag)
    }
}
