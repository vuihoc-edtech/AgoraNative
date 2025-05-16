//
//  ShareManager.swift
//  Flat
//
//  Created by xuyunshi on 2021/12/24.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Foundation
import UIKit
struct ShareInfo {
    let time: String
    let subject: String
    let number: String
    let link: URL
    let title: String

    init(roomDetail: RoomBasicInfo) {
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        time = formatter.string(from: roomDetail.beginTime)
        subject = roomDetail.title
        number = roomDetail.inviteCode.formatterInviteCode
        if roomDetail.isPmi {
            link = URL(string: AgoraNativePlugin.env.webBaseURL + "/join/\(roomDetail.inviteCode)")!
            title = (AuthStore.shared.user?.name ?? "") + localizeStrings("pmiInviteDescribe")
        } else {
            link = URL(string: AgoraNativePlugin.env.webBaseURL + "/join/\(roomDetail.roomUUID)")!
            title = (AuthStore.shared.user?.name ?? "") + localizeStrings("inviteDescribe")
        }
    }

    var description: String {
        let timeStr = localizeStrings("Start Time") + ": " + time
        let subStr = localizeStrings("Room Theme") + ": " + subject
        let numStr = localizeStrings("Room ID") + ": " + number
        let linkStr = localizeStrings("Join Link") + ": " + link.absoluteString
        let des = title + "\n\n" + subStr + "\n" + timeStr + "\n\n" + numStr + "\n" + linkStr
        return des
    }
}

enum ShareManager {
    static func createShareActivityViewController(shareInfo: ShareInfo) -> UIViewController {
        let vc = UIActivityViewController(activityItems: [shareInfo.link, shareInfo.description], applicationActivities: nil)

        vc.excludedActivityTypes = [
            .airDrop,
            .mail,
            .addToReadingList,
            .copyToPasteboard,
        ]
        return vc
    }
}
