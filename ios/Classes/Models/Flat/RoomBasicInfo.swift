//
//  RoomListInfo.swift
//  Flat
//
//  Created by xuyunshi on 2021/10/19.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Fastboard
import Foundation
import RxSwift

extension String {
    var formatterInviteCode: String {
        let isAllNumer = allSatisfy(\.isNumber)
        if !isAllNumer {
            return self
        }
        if count == 11 {
            let i = index(startIndex, offsetBy: 4)
            let j = index(i, offsetBy: 3)

            let r =
                self[startIndex ..< i]
                    +
                    " "
                    +
                    self[i ..< j]
                    +
                    " "
                    +
                    self[j ..< endIndex]
            return String(r)
        }
        return split(every: 3).joined(separator: " ")
    }
}

/// Get from list or by 'ordinary' request
struct RoomBasicInfo: Decodable, Equatable {
    let roomUUID: String
    let periodicUUID: String?

    let ownerUUID: String
    let ownerName: String
    var isOwner: Bool {
        ownerUUID == AuthStore.shared.user?.userUUID ?? ""
    }

    let title: String
    let roomType: ClassRoomType
    let beginTime: Date
    let endTime: Date
    var roomStatus: RoomStartStatus

    let region: String
    let hasRecord: Bool
    let inviteCode: String
    let ownerAvatarURL: String
    let isPmi: Bool
}

extension RoomBasicInfo {
    /// This method can't get periodicUUID
    /// Periodic room info can be fetched either periodicUUID or a inviteUUID
    static func fetchInfoBy(uuid: String, periodicUUID: String?) -> Observable<Self> {
        let request = RoomInfoRequest(roomUUID: uuid)
        return ApiProvider.shared.request(fromApi: request).map {
            let info = $0.roomInfo
            return RoomBasicInfo(roomUUID: uuid,
                                 periodicUUID: periodicUUID,
                                 ownerUUID: info.ownerUUID,
                                 ownerName: info.ownerUserName,
                                 title: info.title,
                                 roomType: info.roomType,
                                 beginTime: info.beginTime,
                                 endTime: info.endTime,
                                 roomStatus: info.roomStatus,
                                 region: info.region,
                                 hasRecord: info.hasRecord,
                                 inviteCode: info.inviteCode,
                                 ownerAvatarURL: "",
                                 isPmi: info.isPmi)
        }
    }

    /// This method can't get periodicUUID
    /// Periodic room info can be fetched either periodicUUID or a inviteUUID
    static func fetchInfoBy(uuid: String, periodicUUID: String?, completion: @escaping ((Result<Self, Error>) -> Void)) {
        let request = RoomInfoRequest(roomUUID: uuid)
        ApiProvider.shared.request(fromApi: request) { result in
            switch result {
            case let .success(raw):
                let info = raw.roomInfo
                let basicInfo = RoomBasicInfo(roomUUID: uuid,
                                              periodicUUID: periodicUUID,
                                              ownerUUID: info.ownerUUID,
                                              ownerName: info.ownerUserName,
                                              title: info.title,
                                              roomType: info.roomType,
                                              beginTime: info.beginTime,
                                              endTime: info.endTime,
                                              roomStatus: info.roomStatus,
                                              region: info.region,
                                              hasRecord: info.hasRecord,
                                              inviteCode: info.inviteCode,
                                              ownerAvatarURL: "",
                                              isPmi: info.isPmi)
                completion(.success(basicInfo))
            case let .failure(error):
                completion(.failure(error))
            }
        }
    }
}

private struct RoomInfoRequest: FlatRequest {
    let roomUUID: String

    var path: String { "/v1/room/info/ordinary" }
    var task: Task { .requestJSONEncodable(encodable: ["roomUUID": roomUUID]) }
    let responseType = RawRoomInfo.self
    var customBaseURL: String? { Env().customBaseUrlFor(roomUUID: roomUUID) }
}

// Middle Struct
private struct RawRoomInfo: Decodable {
    let roomInfo: RoomInfo
}

// Middle Struct
private struct RoomInfo: Decodable {
    let title: String
    let beginTime: Date
    let endTime: Date
    let roomType: ClassRoomType
    var roomStatus: RoomStartStatus
    let hasRecord: Bool
    let ownerUUID: String
    let ownerUserName: String
    let region: String
    let inviteCode: String
    let isPmi: Bool
}
