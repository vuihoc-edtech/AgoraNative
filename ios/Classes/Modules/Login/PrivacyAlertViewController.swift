//
//  PrivacyAlertViewController.swift
//  Flat
//
//  Created by xuyunshi on 2022/5/18.
//  Copyright © 2022 agora.io. All rights reserved.
//

import UIKit

class PrivacyAlertViewController: UIViewController {
    internal init(agreeClick: @escaping (() -> Void),
                  cancelClick: @escaping (() -> Void),
                  alertTitle: String,
                  agreeTitle: String,
                  rejectTitle: String,
                  attributedString: NSAttributedString)
    {
        self.agreeClick = agreeClick
        self.cancelClick = cancelClick
        self.alertTitle = alertTitle
        self.agreeTitle = agreeTitle
        self.rejectTitle = rejectTitle
        self.attributedString = attributedString
        super.init(nibName: nil, bundle: AgoraNativePlugin.pluginBundle)
        modalTransitionStyle = .crossDissolve
        modalPresentationStyle = .overCurrentContext
    }

    let agreeClick: () -> Void
    let cancelClick: () -> Void
    let alertTitle: String
    let agreeTitle: String
    let rejectTitle: String
    let attributedString: NSAttributedString

    @available(*, unavailable)
    required init?(coder _: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    var clickEmptyToCancel = true
    @IBOutlet var textViewHeightConstraint: NSLayoutConstraint!
    @IBOutlet var attributedStringTextView: UITextView!
    @IBOutlet var mainBgView: UIView!
    @IBOutlet var titleLabel: UILabel!
    @IBOutlet var cancelBtn: UIButton!
    @IBOutlet var agreeBtn: FlatGeneralCrossButton!
    override func viewDidLoad() {
        super.viewDidLoad()

        titleLabel.text = alertTitle
        agreeBtn.setTitle(agreeTitle, for: .normal)
        cancelBtn.setTitle(rejectTitle, for: .normal)
        cancelBtn.layer.borderWidth = commonBorderWidth

        titleLabel.textColor = .color(type: .text)
        mainBgView.backgroundColor = .color(type: .background)

        let maxHeight: CGFloat = 280
        let b = attributedString.boundingRect(with: .init(width: maxHeight, height: .infinity), options: .usesLineFragmentOrigin, context: nil)
        textViewHeightConstraint.constant = min(b.height + 14, 340)
        attributedStringTextView.attributedText = attributedString
        attributedStringTextView.linkTextAttributes = [.foregroundColor: UIColor.color(type: .primary), .underlineColor: UIColor.clear]

        let btn = UIButton(type: .custom)
        btn.tag = 1
        view.insertSubview(btn, at: 0)
        btn.addTarget(self, action: #selector(onClickReject(_:)), for: .touchUpInside)
        btn.snp.makeConstraints { $0.edges.equalToSuperview() }
    }

    @IBAction func onClickAgree(_: Any) {
        agreeClick()
    }

    @IBAction func onClickReject(_ sender: UIButton) {
        if sender.tag == 1, !clickEmptyToCancel {
            return
        }
        cancelClick()
    }
}
