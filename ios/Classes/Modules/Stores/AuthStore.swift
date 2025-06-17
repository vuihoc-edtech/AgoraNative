//
//  AuthStore.swift
//  flat
//
//  Created by xuyunshi on 2021/10/14.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Foundation
import UIKit
import RxSwift

let avatarUpdateNotificationName: Notification.Name = .init(rawValue: "avatarUpdateNotification")
let jwtExpireNotificationName: Notification.Name = .init("jwtExpireNotification")

typealias LoginHandler = (Result<User, Error>) -> Void

class AuthStore {
    private let userDefaultKey = "AuthStore_user"

    static let shared = AuthStore()

    
    var unsetDefaultProfileUserUUID: String {
        get {
            (UserDefaults.standard.value(forKey: "unsetDefaultProfileSet") as? String) ?? ""
        }
        set {
            UserDefaults.standard.setValue(newValue, forKey: "unsetDefaultProfileSet")
        }
    }
    
    var disposeBag = DisposeBag()
    
    var isLogin: Bool { user != nil }

    var user: User? {
        didSet {
            flatGenerator.token = user?.token
        }
    }

    func logout() {
        user = nil
    }

    func processBindPhoneSuccess() {
        guard var newUser = user else {
            return
        }
        newUser.hasPhone = true
        processLoginSuccessUserInfo(newUser)
    }

    func processLoginSuccessUserInfo(_ user: User, relogin: Bool = true) {
        do {
            let data = try JSONEncoder().encode(user)
        } catch {
            print("encode user error \(error)")
        }
        self.user = user
//        if relogin {
//            NotificationCenter.default.post(name: loginSuccessNotificationName, object: nil, userInfo: ["user": user])
//            observeFirstJWTExpire()
//        }
    }

    func observeFirstJWTExpire() {
        FlatResponseHandler
            .jwtExpireSignal
            .take(1)
            .observe(on: MainScheduler.instance)
            .subscribe(with: self, onNext: { weakSelf, _ in
                print("post jwt expire notification")
                ApiProvider.shared.cancelAllTasks()
                NotificationCenter.default.post(name: jwtExpireNotificationName, object: nil)
            })
            .disposed(by: disposeBag)
    }

    // MARK: - Update info

    func updateName(_ name: String) {
        user?.name = name
        if let user {
            processLoginSuccessUserInfo(user, relogin: false)
        }
    }

    func updateAvatar(_ url: URL) {
        user?.avatar = url.absoluteString
        if let user {
            processLoginSuccessUserInfo(user, relogin: false)
        }
        NotificationCenter.default.post(name: avatarUpdateNotificationName, object: nil)
    }

    func updateToken(_ token: String) {
        user?.token = token
        if let user {
            processLoginSuccessUserInfo(user)
        }
    }
}
