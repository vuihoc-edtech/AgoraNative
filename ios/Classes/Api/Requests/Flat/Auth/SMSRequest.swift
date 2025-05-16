//
//  SMSRequest.swift
//  flat
//
//  Created by xuyunshi on 2021/10/14.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Foundation

struct SMSRequest: FlatRequest {
    enum SMSLanguageType: String {
        case zh
        case en
    }
    enum Scenario {
        case login(phone: String)
        case bind(phone: String)
        case rebind(phone: String)
        case bindEmail(String)
        case resetPhone(String)
        case resetEmail(String, language: SMSLanguageType)
        case emailRegister(email: String, language: SMSLanguageType)
        case phoneRegister(phone: String)

        var path: String {
            switch self {
            case .rebind:
                return "/v2/user/rebind-phone/send-message"
            case .bindEmail:
                return "/v1/user/bindingEmail/sendMessage"
            case .login:
                return "/v1/login/phone/sendMessage"
            case .bind:
                return "/v1/user/bindingPhone/sendMessage"
            case .resetPhone:
                return "/v2/reset/phone/send-message"
            case .resetEmail:
                return "/v2/reset/email/send-message"
            case .emailRegister:
                return "/v2/register/email/send-message"
            case .phoneRegister:
                return "/v2/register/phone/send-message"
            }
        }
        
        var encodableResult: Encodable {
            switch self {
            case .rebind(phone: let phone): return ["phone": phone]
            case .bindEmail(let email): return ["email": email]
            case .login(phone: let phone): return ["phone": phone]
            case .bind(phone: let phone): return ["phone": phone]
            case .resetPhone(let phone): return ["phone": phone]
            case .resetEmail(let email, language: let l): return ["email": email, "language": l.rawValue]
            case .emailRegister(email: let email, language: let l): return ["email": email, "language": l.rawValue]
            case .phoneRegister(phone: let phone): return ["phone": phone]
            }
        }
    }

    let scenario: Scenario
    var path: String { scenario.path }
    var task: Task { .requestJSONEncodable(encodable: scenario.encodableResult) }
    let responseType = EmptyResponse.self
}
