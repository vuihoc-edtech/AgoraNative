//
//  XCConfiguration.swift
//  flat
//
//  Created by xuyunshi on 2021/10/13.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Foundation

struct Env {
    func value<T>(for key: String) -> T {
        guard let value = Bundle.main.infoDictionary?[key] as? T else {
            fatalError("Invalid or missing Info.plist key: \(key)")
        }
        return value
    }

    var webBaseURL: String {
        "https://web.flat.agora.io"
    }

    var baseURL: String = "https://dev-class-api.rinoedu.ai"

    var build: String {
        "vh"
    }
    ///Mandatory
    var ossAccessKeyId: String {
        "LTAI5tRTgaUQqSs5SDUvLmBA"
    }

    var netlessAppId: String {
        "Q9CKoASEEfCMH5n9aKKKyw/wsn4bnq3RHQkzA"
    }

    var agoraAppId: String {
        "839e5d402e2a4371bd4a788bbab4f2d8"
    }
    ///
    ///
    var version: String = "1.1.1"
    
    var region: String = "sg"

    var joinEarly: TimeInterval {
        let stored = UserDefaults.standard.value(forKey: "joinEarly") as? TimeInterval
        return stored ?? (5 * 60)
    }
    
    func updateJoinEarly(_ newValue: TimeInterval) {
        UserDefaults.standard.setValue(newValue, forKey: "joinEarly")
    }
}
