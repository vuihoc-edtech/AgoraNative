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
    
    private func fetchClassroomViewController(uuid: String, cam: Bool, mic: Bool) -> Single<ClassRoomViewController>
    {
        guard let user = AuthStore.shared.user else { return .error("user not login") }
        let deviceStatusStore = UserDevicePreferredStatusStore(userUUID: user.userUUID)
        let deviceState = DeviceState(mic: mic, camera: cam)
        return RoomPlayInfo.fetchByJoinWith(uuid: uuid)
            .concatMap { p -> Observable<(RoomPlayInfo, RoomBasicInfo)> in
                return RoomBasicInfo.fetchInfoBy(uuid: p.roomUUID, periodicUUID: nil)
                    .map { (p, $0) }
            }
            .map { ClassroomFactory.getClassRoomViewController(withPlayInfo: $0.0, basicInfo: $0.1, deviceStatus: deviceState) }
            .asSingle()
    }
    
    
    func enterClassroomFromFlutter(uuid: String, cam: Bool, mic: Bool,
                                   result: @escaping FlutterResult)
    {
        currentClassroomUUID = uuid
        enterClassDate = Date()
        
        fetchClassroomViewController(uuid: uuid, cam: cam, mic: mic)
            .observe(on: MainScheduler.instance)
            .subscribe(with: self, onSuccess: { _, vc in
                result(1)
                guard let main = AgoraNativePlugin.topViewController() else { return }
                main.present(vc, animated: true)
            }, onFailure: { weakSelf, error in
                weakSelf.currentClassroomUUID = nil
                if let flatError = error as? FlatApiError {
                    result(flatError.rawValue)
                } else {
                    result(-2)
                }
            })
            .disposed(by: rx.disposeBag)
    }
}
