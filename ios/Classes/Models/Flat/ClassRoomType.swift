//
//  ClassType.swift
//  flat
//
//  Created by xuyunshi on 2021/10/15.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Foundation

struct ClassRoomType: RawRepresentable, Codable, Equatable {
    enum RtcStrategy {
        case teacherOrSpeaking
        case all

        func displayingUsers(with users: [RoomUser], ownerRtmUUID: String) -> [RoomUser] {
            switch self {
            case .all: return users
            case .teacherOrSpeaking:
                return users.filter { $0.status.isSpeak || $0.rtmUUID == ownerRtmUUID }
            }
        }
    }
    
    var allowAutoOnstage: Bool {
        if self == .bigClass { return false }
        return true
    }
    
    var whiteboardAlwaysWritable: Bool {
        if self == .bigClass { return false }
        return true
    }

    var maxWritableUsersCount: Int {
        switch self {
        case .bigClass: return 2
        case .oneToOne: return 2
        case .smallClass: return .max
        default:
            return .max
        }
    }

    var rtcStrategy: RtcStrategy {
        if self == .bigClass {
            return .teacherOrSpeaking
        }
        return .all
    }

    static let bigClass = ClassRoomType(rawValue: "BigClass")
    static let smallClass = ClassRoomType(rawValue: "SmallClass")
    static let oneToOne = ClassRoomType(rawValue: "OneToOne")

    var rawValue: String
    init(rawValue: String) {
        self.rawValue = rawValue
    }
}
