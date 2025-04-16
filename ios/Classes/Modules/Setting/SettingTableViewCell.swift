//
//  SettingTableViewCell.swift
//  flat
//
//  Created by xuyunshi on 2021/10/15.
//  Copyright © 2021 agora.io. All rights reserved.
//

import UIKit

class SettingTableViewCell: UITableViewCell {
    @IBOutlet var settingDetailLabel: UILabel!
    @IBOutlet var settingTitleLabel: UILabel!
    @IBOutlet var popOverAnchorView: UIView!
    @IBOutlet var iconImageView: UIImageView!
    @IBOutlet var `switch`: UISwitch!
    @IBOutlet var rightArrowView: UIImageView!

    override func awakeFromNib() {
        super.awakeFromNib()
        selectionStyle = .none
        settingTitleLabel.textColor = .color(type: .text)
        settingDetailLabel.textColor = .color(type: .text)
        backgroundColor = .color(type: .background)
        contentView.backgroundColor = .color(type: .background)

        rightArrowView.tintColor = .color(type: .text)
        iconImageView.tintColor = .color(type: .text)

        addLine(direction: .bottom, color: .borderColor, inset: .init(top: 0, left: 16, bottom: 0, right: 16))
    }
}
