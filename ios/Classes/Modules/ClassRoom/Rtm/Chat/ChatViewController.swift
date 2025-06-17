//
//  ChatViewController.swift
//  Flat
//
//  Created by xuyunshi on 2021/11/16.
//  Copyright © 2021 agora.io. All rights reserved.
//

import DZNEmptyDataSet
import RxCocoa
import RxRelay
import RxSwift
import UIKit

class ChatViewController: UIViewController {
    let noticeCellIdentifier = "noticeCellIdentifier"
    let cellIdentifier = "cellIdentifier"
    let viewModel: ChatViewModel

    let ownerRtmId: String
    let userRtmId: String
    /// If message is banning now
    var isInMessageBan = false {
        didSet {
            banTextButton.isSelected = isInMessageBan
        }
    }

    /// Is message been banned
    var isMessageBaned = false {
        didSet {
            updateDidMessageBan(isMessageBaned)
        }
    }

    var messages: [DisplayMessage] = [] {
        didSet {
            tableView.reloadData()
            let last = tableView.numberOfRows(inSection: 0) - 1
            if last >= 0 {
                tableView.scrollToRow(at: IndexPath(row: last, section: 0), at: .middle, animated: true)
            }
        }
    }

    // MARK: - LifeCycle

    init(viewModel: ChatViewModel,
         userRtmId: String,
         ownerRtmId: String)
    {
        self.ownerRtmId = ownerRtmId
        self.viewModel = viewModel
        self.userRtmId = userRtmId
        super.init(nibName: nil, bundle: AgoraNativePlugin.pluginBundle)
        modalPresentationStyle = .popover
        preferredContentSize = .init(width: UIScreen.main.bounds.width / 2, height: 560)
    }

    @available(*, unavailable)
    required init?(coder _: NSCoder) {
        fatalError()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupViews()
        bind()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        tableView.contentInset = .init(top: topView.bounds.height, left: 0, bottom: inputStackView.bounds.height + 14, right: 0)
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        tableView.reloadEmptyDataSet()
    }

    // MARK: - Private

    func bind() {
        let returnKey = inputTextField.rx.controlEvent(.editingDidEndOnExit)
        let send = Driver.of(returnKey.asDriver(),
                             sendButton.rx.tap.asDriver())
            .merge()

        let output = viewModel.transform(input: .init(sendTap: send,
                                                      textInput: inputTextField.rx.text.orEmpty.asDriver()))

        output.sendMessageEnable
            .drive(sendButton.rx.isEnabled)
            .disposed(by: rx.disposeBag)

        output.sendMessage
            .drive(with: self, onNext: { weakSelf, _ in
                weakSelf.inputTextField.text = nil
                weakSelf.inputTextField.sendActions(for: .valueChanged)
            })
            .disposed(by: rx.disposeBag)

        output.message.asDriver(onErrorJustReturn: [])
            .drive(with: self, onNext: { weakSelf, msgs in
                weakSelf.messages = msgs
            })
            .disposed(by: rx.disposeBag)

        // Outside

        viewModel.isBanned
            .drive(banTextButton.rx.isSelected)
            .disposed(by: rx.disposeBag)

        viewModel.isBanned
            .map { [weak self] ban -> Bool in
                if let isOwner = self?.viewModel.isOwner, isOwner {
                    return false
                } else {
                    return ban
                }
            }
            .drive(with: self, onNext: { weakSelf, ban in
                weakSelf.inputTextField.isEnabled = !ban
                if ban {
                    weakSelf.inputTextField.text = nil
                    weakSelf.inputTextField.sendActions(for: .valueChanged)
                }
                weakSelf.inputTextField.placeholder = ban ? localizeStrings("All banned") : localizeStrings("Say Something...")
            })
            .disposed(by: rx.disposeBag)

        updateBanTextButtonEnable(viewModel.isOwner)
    }

    func setupViews() {
        view.backgroundColor = .classroomChildBG
        view.addSubview(tableView)
        let inputBg = UIView()
        inputBg.backgroundColor = .classroomChildBG
        view.addSubview(inputBg)
        view.addSubview(inputStackView)
        view.addSubview(topView)

        topView.snp.makeConstraints { make in
            make.left.right.top.equalToSuperview()
            make.height.equalTo(40)
        }

        tableView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        view.addLayoutGuide(leftMarginGuide)
        leftMarginGuide.snp.makeConstraints { make in
            make.bottom.height.equalTo(inputStackView)
            make.left.equalToSuperview()
            make.width.equalTo(0)
        }
        inputStackView.snp.makeConstraints { make in
            make.left.equalTo(leftMarginGuide.snp.right)
            make.right.bottom.equalTo(view.safeAreaLayoutGuide)
            make.height.equalTo(48)
        }

        inputBg.snp.makeConstraints { make in
            make.left.right.bottom.equalToSuperview()
            make.height.equalTo(inputStackView)
        }

        let line = UIView()
        line.backgroundColor = .classroomBorderColor
        view.addSubview(line)
        line.snp.makeConstraints { make in
            make.left.right.equalTo(inputStackView)
            make.bottom.equalTo(inputStackView.snp.top)
            make.height.equalTo(1 / UIScreen.main.scale)
        }
    }

    // Call it after view did load
    func updateBanTextButtonEnable(_ enable: Bool) {
        banTextButton.isHidden = !enable
        leftMarginGuide.snp.updateConstraints { make in
            make.width.equalTo(enable ? 0 : 8)
        }
    }

    fileprivate func updateDidMessageBan(_ ban: Bool) {
        sendButton.isEnabled = !ban
        inputTextField.isEnabled = !ban
        if ban {
            inputTextField.text = nil
        }
        inputTextField.placeholder = ban ? localizeStrings("All banned") : localizeStrings("Say Something...")
    }

    // MARK: - Lazy

    lazy var sendButton: UIButton = {
        let button = UIButton(type: .custom)
        if #available(iOS 13.0, *) {
            button.setTraitRelatedBlock { button in
                let disabledColor = UIColor.color(type: .text, .strong).resolvedColor(with: button.traitCollection)
                let normalColor = UIColor.color(type: .text, .weak).resolvedColor(with: button.traitCollection)
                
                button.setImage(UIImage.fromPlugin(named: "send_message")?.tintColor(normalColor), for: .normal)
                button.setImage(UIImage.fromPlugin(named: "send_message")?.tintColor(disabledColor), for: .disabled)
            }
        } else {
            let normalColor = UIColor.color(type: .text, .strong)
            let disabledColor = UIColor.color(type: .text, .weak)
            
            button.setImage(UIImage.fromPlugin(named: "send_message")?.tintColor(normalColor), for: .normal)
            button.setImage(UIImage.fromPlugin(named: "send_message")?.tintColor(disabledColor), for: .disabled)
        }
        button.contentEdgeInsets = .init(top: 0, left: 8, bottom: 0, right: 8)
        return button
    }()

    lazy var leftMarginGuide = UILayoutGuide()

    lazy var inputStackView: UIStackView = {
        let view = UIStackView(arrangedSubviews: [banTextButton, textFieldContainer, sendButton])
        view.axis = .horizontal
        view.distribution = .fill
        sendButton.snp.makeConstraints { $0.width.equalTo(44) }
        return view
    }()

    lazy var textFieldContainer: UIView = {
        let container = UIView()
        container.addSubview(inputTextField)
        inputTextField.snp.makeConstraints { make in
            make.edges.equalToSuperview().inset(UIEdgeInsets(top: 8, left: 0, bottom: 8, right: 0))
        }
        return container
    }()

    lazy var banTextButton: UIButton = {
        let btn = UIButton(type: .custom)
        if #available(iOS 13.0, *) {
            btn.setTraitRelatedBlock { button in
                let normalColor = UIColor.color(type: .text).resolvedColor(with: button.traitCollection)
                let selectedColor = UIColor.color(type: .danger).resolvedColor(with: button.traitCollection)
                
                button.setImage(UIImage.fromPlugin(named: "message_ban")?.tintColor(normalColor), for: .normal)
                button.setImage(UIImage.fromPlugin(named: "message_ban")?.tintColor(selectedColor), for: .selected)
            }
        } else {
            let normalColor = UIColor.color(type: .text)
            let selectedColor = UIColor.color(type: .danger)
            
            btn.setImage(UIImage.fromPlugin(named: "message_ban")?.tintColor(normalColor), for: .normal)
            btn.setImage(UIImage.fromPlugin(named: "message_ban")?.tintColor(selectedColor), for: .selected)
        }

        btn.contentEdgeInsets = .init(top: 0, left: 8, bottom: 0, right: 8)
        return btn
    }()

    lazy var inputTextField: UITextField = {
        let inputTextField = UITextField(frame: .zero)
        inputTextField.backgroundColor = .classroomChildBG
        inputTextField.textColor = .color(type: .text, .strong)
        inputTextField.clipsToBounds = true
        inputTextField.font = .systemFont(ofSize: 14)
        inputTextField.placeholder = localizeStrings("Say Something...")
        inputTextField.returnKeyType = .send
        inputTextField.leftView = UIView(frame: .init(x: 0, y: 0, width: 8, height: 8))
        inputTextField.leftViewMode = .always
        return inputTextField
    }()

    lazy var topView: UIView = {
        let view = UIView(frame: .zero)
        view.backgroundColor = .classroomChildBG
        let leftIcon = UIImageView()
        if #available(iOS 13.0, *) {
            view.setTraitRelatedBlock { [weak leftIcon] v in
                let tintColor = UIColor.color(type: .text, .strong).resolvedColor(with: v.traitCollection)
                leftIcon?.image = UIImage.fromPlugin(named: "chat")?.tintColor(tintColor)
            }
        } else {
            // Fallback for iOS 12: Use the static color
            let tintColor = UIColor.color(type: .text, .strong)
            leftIcon.image = UIImage.fromPlugin(named: "chat")?.tintColor(tintColor)
        }
        leftIcon.contentMode = .scaleAspectFit
        view.addSubview(leftIcon)
        leftIcon.snp.makeConstraints { make in
            make.width.height.equalTo(24)
            make.centerY.equalToSuperview()
            make.left.equalToSuperview().inset(8)
        }

        let topLabel = UILabel(frame: .zero)
        topLabel.text = localizeStrings("Chat")
        topLabel.textColor = .color(type: .text, .strong)
        topLabel.font = .systemFont(ofSize: 14, weight: .semibold)
        view.addSubview(topLabel)
        topLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalToSuperview().inset(40)
        }
        view.addLine(direction: .bottom, color: .classroomBorderColor)
        return view
    }()

    lazy var tableView: UITableView = {
        let view = UITableView(frame: .zero, style: .plain)
        view.backgroundColor = .classroomChildBG
        view.contentInsetAdjustmentBehavior = .never
        view.separatorStyle = .none
        view.register(ChatTableViewCell.self, forCellReuseIdentifier: cellIdentifier)
        view.register(ChatNoticeTableViewCell.self, forCellReuseIdentifier: noticeCellIdentifier)
        view.delegate = self
        view.dataSource = self
        view.emptyDataSetDelegate = self
        view.emptyDataSetSource = self
        return view
    }()
}

extension ChatViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let message = messages[indexPath.row]
        switch message {
        case let .user(message: message, info: info):
            let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as! ChatTableViewCell
            let formatter = DateFormatter()
            formatter.timeStyle = .short
            formatter.dateStyle = .none
            let dateStr = formatter.string(from: message.time)
            cell.update(nickName: info.name,
                        text: message.text,
                        time: dateStr,
                        avatar: info.avatar,
                        isTeach: message.userId == ownerRtmId,
                        style: message.userId == userRtmId ? .self : .other)
            return cell
        case let .notice(notice):
            let cell = tableView.dequeueReusableCell(withIdentifier: noticeCellIdentifier, for: indexPath) as! ChatNoticeTableViewCell
            cell.labelView.label.text = notice
            return cell
        }
    }

    func numberOfSections(in _: UITableView) -> Int {
        1
    }

    func tableView(_: UITableView, numberOfRowsInSection _: Int) -> Int {
        messages.count
    }

    func tableView(_: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let message = messages[indexPath.row]
        switch message {
        case let .user(message: message, _):
            let width = view.bounds.width - (2 * ChatTableViewCell.textMargin) - ChatTableViewCell.textEdge.left - ChatTableViewCell.textEdge.right
            let textSize = message.text.boundingRect(with: .init(width: width,
                                                                 height: .greatestFiniteMagnitude),
                                                     options: [.usesLineFragmentOrigin, .usesFontLeading],
                                                     attributes: ChatTableViewCell.textAttribute,
                                                     context: nil).size
            let textContainerHeight = textSize.height + ChatTableViewCell.textEdge.top + ChatTableViewCell.textEdge.bottom
            return ceil(max(32, textContainerHeight)) + ChatTableViewCell.textTopMargin
        case .notice:
            return 26.5 + 12
        }
    }
}

// MARK: - EmptyData

extension ChatViewController: DZNEmptyDataSetSource, DZNEmptyDataSetDelegate {
    func title(forEmptyDataSet _: UIScrollView) -> NSAttributedString? {
        nil
    }

    func backgroundColor(forEmptyDataSet _: UIScrollView!) -> UIColor! {
        .classroomChildBG
    }

    func image(forEmptyDataSet _: UIScrollView) -> UIImage? {
        UIImage(named: "message_empty", in: AgoraNativePlugin.pluginBundle, compatibleWith: traitCollection)
    }
    
    func verticalOffset(forEmptyDataSet scrollView: UIScrollView!) -> CGFloat {
        -tableView.contentInset.top
    }

    func emptyDataSetShouldAllowScroll(_: UIScrollView) -> Bool {
        true
    }
}
