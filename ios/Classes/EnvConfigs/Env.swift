//
//  XCConfiguration.swift
//  flat
//
//  Created by xuyunshi on 2021/10/13.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Foundation

enum LoginType: String, CaseIterable {
    case email
    case phone
    case github
    case apple
    case wechat
    case google
}

struct Env {
    struct ServerGroupItem: Decodable {
        fileprivate let apiURL: String
        let classroomInviteCode: Int
        let classroomUUIDPrefix: String
        var baseURL: String {
            "https://\(apiURL)"
        }
    }

    enum Region: String {
        case CN
        case US
    }

    func value<T>(for key: String) -> T {
        guard let value = Bundle.main.infoDictionary?[key] as? T else {
            fatalError("Invalid or missing Info.plist key: \(key)")
        }
        return value
    }

    var disabledLoginTypes: [LoginType] {
        [.wechat]
    }

    var preferPhoneAccount: Bool {
        false
    }

    var forceBindPhone: Bool {
        false
    }

    var region: Region {
        .US
    }
    
    var appUpdateCheckURL: URL {
        .init(string: "https://flat-storage-sg.oss-ap-southeast-1.aliyuncs.com/versions/latest/stable/iOS/ios_latest.json")!
    }
    
    var useCnSpecialAgreement: Bool {
        false
    }

    var serviceURL: URL? {
        .init(string: "https://flat.agora.io/service.html")
    }
    
    var privacyURL: URL? {
        .init(string: "https://flat.agora.io/privacy.html")
    }
    
    var webBaseURL: String {
        "https://web.flat.agora.io"
    }

    var baseURL: String {
       return "https://api.flat.agora.io"
    }

    var name: String {
        Bundle.main.infoDictionary?["CFBundleName"] as? String ?? ""
    }

    var servers: [ServerGroupItem] {
        do {
            let str = "[{\"classroomInviteCode\": 1, \"apiURL\": \"api.flat.apprtc.cn\", \"classroomUUIDPrefix\": \"CN-\"},{\"classroomInviteCode\": 2, \"apiURL\": \"api.flat.agora.io\", \"classroomUUIDPrefix\": \"SG-\"}]"
            if let data = str.data(using: .utf8) {
                let items = try JSONDecoder().decode([ServerGroupItem].self, from: data)
                return items
            }
        } catch {
            // Prevent it by unit test. So do nothing.
        }
        return []
    }

    var version: String {
        "1.1.1"
    }

    var build: String {
        "vh"
    }

    var ossAccessKeyId: String {
        "LTAI5tMwHQ1xyroeneA9XLh4"
    }

    var netlessAppId: String {
        "cFjxAJjiEeuUQ0211QCRBw/kndLTOWdG2qYcQ"
    }

    var agoraAppId: String {
        "931b86d6781e49a2a255db4ce6e8e804"
    }

    var slsProject: String {
        "flat-sg"
    }

    var slsEndpoint: String {
        "https://ap-southeast-1.log.aliyuncs.com"
    }

    var slsSk: String {
        Bundle.main.infoDictionary?["SLS_SK"] as? String ?? ""
    }

    var slsAk: String {
        Bundle.main.infoDictionary?["SLS_AK"] as? String ?? ""
    }
    
    var joinEarly: TimeInterval {
        let stored = UserDefaults.standard.value(forKey: "joinEarly") as? TimeInterval
        return stored ?? (5 * 60)
    }
    
    func updateJoinEarly(_ newValue: TimeInterval) {
        UserDefaults.standard.setValue(newValue, forKey: "joinEarly")
    }
}

private let googleAuthBaseUrl = "https://accounts.google.com/o/oauth2/v2/auth"
private let githubAuthBaseUrl = "https://github.com/login/oauth/authorize"
extension Env {
    func githubBindingURLWith(authUUID uuid: String) -> URL {
//        let redirectUri = baseURL + "/v1/login/github/callback/binding"
//        let queryString = "?client_id=\(githubClientId)&redirect_uri=\(redirectUri)&state=\(uuid)"
//        let urlString = githubAuthBaseUrl + queryString
        return URL(string: "")!
    }

    func githubLoginURLWith(authUUID uuid: String) -> URL {
//        let redirectUri = baseURL + "/v1/login/github/callback"
//        let queryString = "?client_id=\(githubClientId)&redirect_uri=\(redirectUri)&state=\(uuid)"
//        let urlString = githubAuthBaseUrl + queryString
        return URL(string: "")!
    }

    func googleBindingURLWith(authUUID uuid: String) -> URL {
//        let redirectUrl = baseURL + "/v1/user/binding/platform/google"
//        let scope = ["openid", "https://www.googleapis.com/auth/userinfo.profile"].joined(separator: " ").addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
//        let queryString = "?response_type=code&access_type=online&scope=\(scope)&client_id=\(googleClientId)&redirect_uri=\(redirectUrl)&state=\(uuid)"
//        let urlString = googleAuthBaseUrl + queryString
        return URL(string: "")!
    }

    func googleLoginURLWith(authUUID uuid: String) -> URL {
//        let redirectUrl = baseURL + "/v1/login/google/callback"
//        let scope = ["openid", "https://www.googleapis.com/auth/userinfo.profile"].joined(separator: " ").addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
//        let queryString = "?response_type=code&access_type=online&scope=\(scope)&client_id=\(googleClientId)&redirect_uri=\(redirectUrl)&state=\(uuid)"
//        let urlString = googleAuthBaseUrl + queryString
        return URL(string: "")!
    }
}

extension Env {
    var containsSlsInfo: Bool {
        !slsSk.isEmpty && !slsAk.isEmpty && !slsEndpoint.isEmpty && !slsProject.isEmpty
    }
}

extension Env {
    func customBaseUrlFor(roomUUID: String) -> String? {
        let isAllNumer = roomUUID.allSatisfy(\.isNumber)
        if isAllNumer, roomUUID.count == 10 {
            return servers.first(where: { $0.classroomUUIDPrefix == "CN-"})?.baseURL // Old invite code. Using CN.
        }
        for server in servers {
            if isAllNumer {
                if roomUUID.hasPrefix(server.classroomInviteCode.description) {
                    return server.baseURL
                }
            }
            if roomUUID.hasPrefix(server.classroomUUIDPrefix) {
                return server.baseURL
            }
        }
        return nil
    }
    
    func isCrossRegion(roomUUID: String) -> Bool {
        if let url = customBaseUrlFor(roomUUID: roomUUID) {
            return url != baseURL
        }
        return false
    }
}
