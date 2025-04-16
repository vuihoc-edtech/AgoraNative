//
//  InviteViewController.swift
//  Flat
//
//  Created by xuyunshi on 2022/11/21.
//  Copyright © 2022 agora.io. All rights reserved.
//

import UIKit

class InviteViewController: UIViewController {
    override var prefersStatusBarHidden: Bool { presentingViewController?.prefersStatusBarHidden ?? false }
    
    let shareInfo: ShareInfo

    init(shareInfo: ShareInfo) {
        self.shareInfo = shareInfo
        super.init(nibName: nil, bundle: AgoraNativePlugin.pluginBundle)
        modalPresentationStyle = .overCurrentContext
        modalTransitionStyle = .crossDissolve
    }

    @available(*, unavailable)
    required init?(coder _: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupViews()

        let kvs: [(String, String)] = [
            (localizeStrings("Room Theme"), shareInfo.subject),
            (localizeStrings("Room ID"), shareInfo.number),
            (localizeStrings("Start Time"), shareInfo.time),
            (localizeStrings("Join Link"), shareInfo.link.absoluteString),
        ]
        for (k, v) in kvs {
            let item = createDisplayItem(title: k, detail: v)
            mainStackView.addArrangedSubview(item)
        }
    }

    // MARK: - Private

    func setupViews() {
        let emptyBtn = UIButton()
        view.addSubview(emptyBtn)
        emptyBtn.addTarget(self, action: #selector(onClickClose), for: .touchUpInside)
        emptyBtn.snp.makeConstraints { $0.edges.equalToSuperview() }

        view.backgroundColor = UIColor.black.withAlphaComponent(0.3)
        view.addSubview(contentView)
        contentView.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.width.height.lessThanOrEqualToSuperview().inset(16)
            make.width.equalTo(480).priority(.high)
        }
        contentView.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.centerY.equalTo(28)
            make.width.lessThanOrEqualToSuperview().inset(66)
        }
        _ = seperatorLine
        contentView.addSubview(closeButton)
        closeButton.snp.makeConstraints { make in
            make.right.top.equalToSuperview()
            make.width.height.equalTo(56)
        }
        contentView.addSubview(mainStackView)
        contentView.addSubview(buttonsStackView)
        buttonsStackView.snp.makeConstraints { make in
            make.bottom.left.right.equalToSuperview().inset(16)
        }
        buttonsStackView.arrangedSubviews.forEach { i in
            i.snp.makeConstraints { make in
                make.height.equalTo(44)
            }
        }

        mainStackView.snp.makeConstraints { make in
            make.left.right.equalToSuperview().inset(16)
            make.top.equalToSuperview().inset(56 + 16)
            make.bottom.equalTo(buttonsStackView.snp.top).offset(-16)
        }
    }

    func createDisplayItem(title: String, detail: String) -> UIView {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .regular)
        label.textColor = .color(type: .text, .weak)
        label.textAlignment = .left
        label.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        label.text = title

        let detailLabel = UILabel()
        detailLabel.font = .systemFont(ofSize: 14, weight: .regular)
        detailLabel.textColor = .color(type: .text)
        detailLabel.text = detail
        detailLabel.textAlignment = .right
        detailLabel.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        detailLabel.numberOfLines = 0
        let stack = UIStackView(arrangedSubviews: [label, detailLabel])
        stack.spacing = 4
        stack.distribution = .fill
        stack.alignment = .top
        return stack
    }

    // MARK: - Action

    @objc
    func onClickClose() {
        dismiss(animated: true)
    }

    @objc
    func onClickMore(_ sender: UIButton) {
        let vc = ShareManager.createShareActivityViewController(shareInfo: shareInfo)
        if sender.window?.traitCollection.hasCompact ?? true {
            present(vc, animated: true)
        } else {
            popoverViewController(viewController: vc, fromSource: sender)
        }
    }

    @objc
    func onClickCopy() {
        UIPasteboard.general.string = shareInfo.description
        toast(localizeStrings("Copy Success"))
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            guard let self else { return }
            self.dismiss(animated: true)
        }
    }

    // MARK: - Lazy


    lazy var seperatorLine = contentView.addLine(direction: .top, color: .borderColor, inset: .init(top: 56, left: 0, bottom: 0, right: 0))
    
    lazy var contentView: UIView = {
        let view = UIView()
        view.backgroundColor = .color(type: .background)
        view.clipsToBounds = true
        view.layer.cornerRadius = 6
        return view
    }()

    lazy var closeButton: UIButton = {
        let closeButton = UIButton(type: .custom)
        closeButton.setImage(UIImage.fromPlugin(named: "close-bold"), for: .normal)
        closeButton.tintColor = .color(type: .text)
        closeButton.addTarget(self, action: #selector(onClickClose), for: .touchUpInside)
        return closeButton
    }()

    lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .semibold)
        label.textColor = .color(type: .text, .strong)
        label.text = shareInfo.title
        label.numberOfLines = 0
        label.textAlignment = .center
        return label
    }()

    lazy var copyBtn: SpringButton = {
        let btn = SpringButton()
        btn.setTitle(localizeStrings("Copy Invitation"), for: .normal)
        btn.backgroundColor = .color(type: .primary)
        btn.clipsToBounds = true
        btn.layer.cornerRadius = 6
        btn.addTarget(self, action: #selector(onClickCopy), for: .touchUpInside)
        return btn
    }()

    lazy var shareMoreBtn: SpringButton = {
        let btn = SpringButton()
        btn.setTitle(localizeStrings("ShareMore"), for: .normal)
        btn.setTraitRelatedBlock { btn in
            let borderColor = UIColor.color(light: .grey3, dark: .grey6).resolvedColor(with: btn.traitCollection)
            let titleColor = UIColor.color(light: .grey6, dark: .grey3).resolvedColor(with: btn.traitCollection)
            btn.layer.borderColor = borderColor.cgColor
            btn.setTitleColor(titleColor
                .resolvedColor(with: btn.traitCollection),
                for: .normal)
        }
        btn.addTarget(self, action: #selector(onClickMore), for: .touchUpInside)
        btn.layer.borderWidth = commonBorderWidth
        btn.clipsToBounds = true
        btn.layer.cornerRadius = 6
        return btn
    }()

    lazy var buttonsStackView: UIStackView = {
        let view = UIStackView(arrangedSubviews: [shareMoreBtn, copyBtn])
        view.axis = .vertical
        view.spacing = 8
        return view
    }()

    lazy var mainStackView: UIStackView = {
        let view = UIStackView(arrangedSubviews: [])
        view.axis = .vertical
        view.spacing = 8
        return view
    }()
}
