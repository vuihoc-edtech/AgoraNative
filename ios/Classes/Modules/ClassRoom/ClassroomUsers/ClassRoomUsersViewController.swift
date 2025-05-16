//
//  ClassRoomUsersViewController.swift
//  Flat
//
//  Created by xuyunshi on 2021/11/17.
//  Copyright © 2021 agora.io. All rights reserved.
//

import RxCocoa
import RxDataSources
import RxSwift
import UIKit

let classRoomUserNameWidth: CGFloat = 144
let classRoomNormalItemWidth: CGFloat = 100
class ClassRoomUsersViewController: UIViewController {
    let cellIdentifier = "cellIdentifier"

    let userUUID: String
    let roomOwnerRtmUUID: String
    let isOwner: Bool

    let whiteboardTap: PublishRelay<RoomUser> = .init()
    let onStageTap: PublishRelay<RoomUser> = .init()
    let raiseHandTap: PublishRelay<RoomUser> = .init()
    let cameraTap: PublishRelay<RoomUser> = .init()
    let micTap: PublishRelay<RoomUser> = .init()
    let stopInteractingTap: PublishRelay<Void> = .init()
    let allMuteTap: PublishRelay<Void> = .init()

    var teacher: RoomUser? {
        didSet {
            guard teacher != oldValue else { return }
            if let teacher {
                teachAvatarImageView.kf.setImage(with: teacher.avatarURL)
                teacherLabel.text = localizeStrings("Teacher") + ": " + teacher.name
                teacherOfflineLabel.isHidden = teacher.isOnline
            }
            tableView.reloadData()
        }
    }

    var users: Observable<[RoomUser]>? {
        didSet {
            guard let users else { return }

            let ownerId = roomOwnerRtmUUID
            let displayUsers = users
                .do(onNext: { [weak self] users in
                    self?.teacher = users.first(where: { $0.rtmUUID == ownerId })
                })
                .map { $0.filter { user in user.rtmUUID != ownerId } }
                .asDriver(onErrorJustReturn: [])

            displayUsers
                .map { localizeStrings("Students") + " (\($0.count))" }
                .asDriver()
                .drive(studentCountLabel.rx.text)
                .disposed(by: rx.disposeBag)

            let dataSource = RxTableViewSectionedAnimatedDataSource<RoomUserRxSectionData> { [weak self] _, tableView, _, item in
                guard let self else { return UITableViewCell() }
                let cell = tableView.dequeueReusableCell(withIdentifier: self.cellIdentifier) as! RoomUserTableViewCell
                self.config(cell: cell, user: item)
                return cell
            }
            dataSource.animationConfiguration = .init(insertAnimation: .automatic,
                                                      reloadAnimation: .none,
                                                      deleteAnimation: .left)
            
            displayUsers
                .map { users -> [RoomUserRxSectionData] in
                    [RoomUserRxSectionData(items: users)]
                }
                .drive(tableView.rx.items(dataSource: dataSource))
                .disposed(by: rx.disposeBag)
            

            if !isOwner {
                stopInteractingButton.isHidden = true
                allMuteButton.isHidden = true
            }
        }
    }

    // MARK: - LifeCycle

    init(userUUID: String,
         roomOwnerRtmUUID: String)
    {
        self.roomOwnerRtmUUID = roomOwnerRtmUUID
        self.userUUID = userUUID
        isOwner = roomOwnerRtmUUID == userUUID
        super.init(nibName: nil, bundle: AgoraNativePlugin.pluginBundle)

        preferredContentSize = .init(width: classRoomUserNameWidth + classRoomNormalItemWidth * 5, height: 560)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        closeButton.isHidden = modalPresentationStyle == .popover
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

    // MARK: - Private

    func bind() {
        stopInteractingButton.rx.tap
            .bind(to: stopInteractingTap)
            .disposed(by: rx.disposeBag)

        allMuteButton.rx.tap
            .bind(to: allMuteTap)
            .disposed(by: rx.disposeBag)

        tableView
            .rx.setDelegate(self)
            .disposed(by: rx.disposeBag)
    }

    func setupViews() {
        view.backgroundColor = .classroomChildBG
        view.addSubview(tableView)
        view.addSubview(topView)
        topView.snp.makeConstraints { make in
            make.left.right.top.equalTo(view.safeAreaLayoutGuide)
            make.height.equalTo(40)
        }
        tableView.snp.makeConstraints { make in
            make.top.equalTo(topView.snp.bottom)
            make.left.right.bottom.equalTo(view.safeAreaLayoutGuide)
        }
    }

    func config(cell: RoomUserTableViewCell, user: RoomUser) {
        let isUserSelf = user.rtmUUID == userUUID
        cell.update(user: user, isUserSelf: isUserSelf, isOwner: isOwner)
        cell.clickHandler = { [weak self] type in
            guard let self else { return }
            switch type {
            case .camera:
                self.cameraTap.accept(user)
            case .mic:
                self.micTap.accept(user)
            case .onStage:
                self.onStageTap.accept(user)
            case .whiteboard:
                self.whiteboardTap.accept(user)
            case .raiseHand:
                self.raiseHandTap.accept(user)
            }
            
            // After a while. Try check value was actually updated.
            // If not, update with the old value.
            // To avoid some command was rejected by stateHandler.
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) { [weak cell, weak self] in
                guard let cell, let self, let cache = cell.cacheUser else { return }
                if cache.rtmUUID == user.rtmUUID { // Still the same cell.
                    if cache == user {
                        cell.update(user: cache, isUserSelf: isUserSelf, isOwner: self.isOwner)
                    }
                }
            }
        }
    }

    @objc func onClickClose() {
        dismiss(animated: true)
    }

    // MARK: - Lazy

    lazy var topView: UIView = {
        let view = UIView(frame: .zero)
        view.backgroundColor = .classroomChildBG
        let topLabel = UILabel(frame: .zero)
        topLabel.text = localizeStrings("User List")
        topLabel.textColor = .color(type: .text, .strong)
        topLabel.font = .systemFont(ofSize: 14, weight: .semibold)
        view.addSubview(topLabel)
        topLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalToSuperview().inset(40)
        }

        let leftIcon = UIImageView()
        if #available(iOS 13.0, *) {
            view.setTraitRelatedBlock { [weak leftIcon] v in
                let tintColor = UIColor.color(type: .text, .strong).resolvedColor(with: v.traitCollection)
                leftIcon?.image = UIImage.fromPlugin(named: "users")?.tintColor(tintColor)
            }
        } else {
            // Fallback for iOS 12 (no traitCollection or dynamic colors)
            let tintColor = UIColor.color(type: .text, .strong)
            leftIcon.image = UIImage.fromPlugin(named: "users")?.tintColor(tintColor)
        }

        leftIcon.contentMode = .scaleAspectFit
        view.addSubview(leftIcon)
        leftIcon.snp.makeConstraints { make in
            make.width.height.equalTo(24)
            make.centerY.equalToSuperview()
            make.left.equalToSuperview().inset(8)
        }

        view.addSubview(closeButton)
        closeButton.snp.makeConstraints { make in
            make.right.top.bottom.equalToSuperview()
            make.width.equalTo(66)
        }

        view.addLine(direction: .bottom, color: .borderColor)
        return view
    }()

    lazy var closeButton: UIButton = {
        let closeButton = UIButton(type: .custom)
        closeButton.setImage(UIImage.fromPlugin(named: "close-bold"), for: .normal)
        closeButton.tintColor = .color(type: .text)
        closeButton.addTarget(self, action: #selector(onClickClose), for: .touchUpInside)
        return closeButton
    }()

    lazy var teacherLabel: UILabel = {
        let label = UILabel()
        label.textColor = .color(type: .text)
        label.font = .systemFont(ofSize: 14)
        return label
    }()

    lazy var stopInteractingButton: UIButton = {
        let btn = UIButton(type: .custom)
        btn.titleLabel?.font = .systemFont(ofSize: 12)
        btn.setTitleColor(.color(type: .text), for: .normal)
        btn.setTitle(localizeStrings("Stop Interacting"), for: .normal)
        btn.contentEdgeInsets = .init(top: 12, left: 12, bottom: 12, right: 12)
        btn.layer.borderColor = UIColor.borderColor.cgColor
        btn.layer.borderWidth = commonBorderWidth
        return btn
    }()

    lazy var allMuteButton: UIButton = {
        let btn = UIButton(type: .custom)
        btn.titleLabel?.font = .systemFont(ofSize: 12)
        btn.setTitleColor(.color(type: .text), for: .normal)
        btn.setTitle(localizeStrings("All mute"), for: .normal)
        btn.contentEdgeInsets = .init(top: 12, left: 12, bottom: 12, right: 12)
        btn.layer.borderColor = UIColor.borderColor.cgColor
        btn.layer.borderWidth = commonBorderWidth
        return btn
    }()

    lazy var teachAvatarImageView: UIImageView = {
        let view = UIImageView()
        view.clipsToBounds = true
        view.contentMode = .scaleAspectFill
        view.layer.cornerRadius = 12
        return view
    }()

    lazy var globalOperationStackView: UIStackView = {
        let view = UIStackView(arrangedSubviews: [stopInteractingButton, allMuteButton])
        view.axis = .horizontal
        view.spacing = 12
        view.distribution = .fillProportionally
        return view
    }()

    lazy var teacherOfflineLabel: UILabel = {
        let statusLabel = UILabel()
        statusLabel.font = .systemFont(ofSize: 12)
        statusLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
        statusLabel.text = "(\(localizeStrings("offline")))"
        statusLabel.textColor = .systemRed
        return statusLabel
    }()
    
    lazy var teacherNameStackView: UIStackView = {
        let nameStack = UIStackView(arrangedSubviews: [teacherLabel, teacherOfflineLabel])
        nameStack.axis = .vertical
        nameStack.distribution = .fill
        nameStack.spacing = 2
        return nameStack
    }()
    
    lazy var teacherInfoView: UIView = {
        let view = UIView()
        view.addSubview(teachAvatarImageView)
        view.addSubview(teacherNameStackView)
        view.addSubview(globalOperationStackView)
        teachAvatarImageView.snp.makeConstraints { make in
            make.left.equalToSuperview().inset(16)
            make.top.equalToSuperview().inset(12)
            make.width.height.equalTo(24)
        }
        teacherNameStackView.snp.makeConstraints { make in
            make.left.equalToSuperview().inset(48)
            make.centerY.equalTo(teachAvatarImageView)
            make.right.lessThanOrEqualTo(stopInteractingButton.snp.left).offset(-10)
        }
        globalOperationStackView.snp.makeConstraints { make in
            make.right.equalTo(view.safeAreaLayoutGuide).inset(12)
            make.centerY.equalTo(teachAvatarImageView)
            make.height.equalTo(28)
        }
        stopInteractingButton.layer.cornerRadius = 14
        allMuteButton.layer.cornerRadius = 14
        return view
    }()

    lazy var teacherHeaderView: UITableViewHeaderFooterView = {
        let view = UITableViewHeaderFooterView()
        view.contentView.backgroundColor = .classroomChildBG
        let stack = UIStackView(arrangedSubviews: [teacherInfoView, headerItemStackView])
        stack.axis = .vertical
        stack.distribution = .fill
        view.addSubview(stack)
        stack.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        headerItemStackView.snp.makeConstraints { make in
            make.height.equalTo(40)
        }
        return view
    }()

    func createHeaderItem(title: String) -> UILabel {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14)
        label.textColor = .color(type: .text)
        label.text = title
        return label
    }

    lazy var studentCountLabel = createHeaderItem(title: localizeStrings("Students"))

    lazy var headerItemStackView: UIStackView = {
        func wrapView(_ v: UIView) -> UIView {
            let view = UIView()
            view.addSubview(v)
            v.snp.makeConstraints { make in
                make.left.equalToSuperview().inset(16)
                make.centerY.equalToSuperview()
                make.right.lessThanOrEqualToSuperview()
            }
            return view
        }
        
        let view = UIStackView(arrangedSubviews: [wrapView(studentCountLabel),
                                                  wrapView(createHeaderItem(title: localizeStrings("On/Off stage"))),
                                                  wrapView(createHeaderItem(title: localizeStrings("Whiteboard Permissions"))),
                                                  wrapView(createHeaderItem(title: localizeStrings("Camera"))),
                                                  wrapView(createHeaderItem(title: localizeStrings("Mic"))),
                                                  wrapView(createHeaderItem(title: localizeStrings("Raised Hand")))])
        view.axis = .horizontal
        view.distribution = .fillProportionally
        view.backgroundColor = .color(light: .init(hexString: "F9F9F9"), dark: .grey8)
        view.arrangedSubviews.enumerated().forEach { (index, v) in
            v.snp.makeConstraints { $0.width.equalTo(index == 0 ? classRoomUserNameWidth : classRoomNormalItemWidth) }
        }
        return view
    }()

    lazy var tableView: UITableView = {
        let view = UITableView(frame: .zero, style: .plain)
        view.backgroundColor = .classroomChildBG
        view.contentInsetAdjustmentBehavior = .never
        view.separatorStyle = .none
        view.register(RoomUserTableViewCell.self, forCellReuseIdentifier: cellIdentifier)
        view.rowHeight = 56
        if #available(iOS 15.0, *) {
            // F apple
            view.sectionHeaderTopPadding = 0
        } else {}
        return view
    }()
}

extension ClassRoomUsersViewController: UITableViewDelegate {
    func tableView(_: UITableView, heightForHeaderInSection _: Int) -> CGFloat {
        if teacher == nil { return 40 }
        return 88
    }

    func tableView(_: UITableView, viewForHeaderInSection _: Int) -> UIView? {
        teacherInfoView.isHidden = teacher == nil
        return teacherHeaderView
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }
}
