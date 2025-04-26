//
//  ClassRoomSettingViewController.swift
//  Flat
//
//  Created by xuyunshi on 2021/11/15.
//  Copyright © 2021 agora.io. All rights reserved.
//

import RxCocoa
import RxRelay
import RxSwift
import UIKit

let classroomSettingNeedToggleCameraNotification = Notification.Name("classroomSettingNeedToggleCameraNotification")
let classroomSettingNeedToggleFrontMirrorNotification = Notification.Name("classroomSettingNeedToggleFrontMirrorNotification")
class ClassRoomSettingViewController: UIViewController {
    enum SettingControlType {
        case camera
        case cameraDirection
        case mic
        case videoArea
        case shortcut
        case mirror

        var description: String {
            switch self {
            case .mirror:
                return localizeStrings("Mirror")
            case.cameraDirection:
                return localizeStrings("CameraDirection")
            case .camera:
                return localizeStrings("Camera")
            case .mic:
                return localizeStrings("Mic")
            case .videoArea:
                return localizeStrings("Video Area")
            case .shortcut:
                return localizeStrings("PreferencesSetting")
            }
        }
    }

    let cameraPublish: PublishRelay<Void> = .init()
    let micPublish: PublishRelay<Void> = .init()
    let videoAreaPublish: PublishRelay<Void> = .init()
    var preferencePublish: PublishRelay<Void> = .init()

    let cellIdentifier = "cellIdentifier"

    let deviceUpdateEnable: BehaviorRelay<Bool>
    let cameraOn: BehaviorRelay<Bool>
    let micOn: BehaviorRelay<Bool>
    let videoAreaOn: BehaviorRelay<Bool>
    var models: [[SettingControlType]] = []
    fileprivate var frontMirror = ClassroomDefaultConfig.frontCameraMirror {
        didSet {
            // Only update on front camera
            NotificationCenter.default.post(.init(name: classroomSettingNeedToggleFrontMirrorNotification))
        }
    }
    fileprivate var isCameraFront = ClassroomDefaultConfig.frontCameraMirror {
        didSet {
            reloadModels()
            NotificationCenter.default.post(.init(name: classroomSettingNeedToggleCameraNotification))
        }
    }
    
    func reloadModels() {
        if cameraOn.value {
            if isCameraFront {
                models = [[.shortcut], [.camera, .cameraDirection, .mirror], [.mic], [.videoArea]]
            } else {
                models = [[.shortcut], [.camera, .cameraDirection], [.mic], [.videoArea]]
            }
        } else {
            models = [[.shortcut], [.camera], [.mic], [.videoArea]]
        }
        tableView.reloadData()
    }

    // MARK: - LifeCycle

    init(cameraOn: Bool,
         micOn: Bool,
         videoAreaOn: Bool,
         deviceUpdateEnable: Bool)
    {
        self.cameraOn = .init(value: cameraOn)
        self.micOn = .init(value: micOn)
        self.videoAreaOn = .init(value: videoAreaOn)
        self.deviceUpdateEnable = .init(value: deviceUpdateEnable)
        super.init(nibName: nil, bundle: AgoraNativePlugin.pluginBundle)
        modalPresentationStyle = .popover
        preferredContentSize = .init(width: 320, height: 480)
    }

    @available(*, unavailable)
    required init?(coder _: NSCoder) {
        fatalError()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupViews()

        Observable.of(cameraOn, micOn, videoAreaOn)
            .merge()
            .subscribe(with: self) { weakSelf, _ in
                weakSelf.reloadModels()
            }
            .disposed(by: rx.disposeBag)
    }

    // MARK: - Private

    func setupViews() {
        view.backgroundColor = .classroomChildBG
        view.addSubview(tableView)
        view.addSubview(topView)
        let topViewHeight: CGFloat = 40
        topView.snp.makeConstraints { make in
            make.left.right.top.equalToSuperview()
            make.height.equalTo(topViewHeight)
        }

        tableView.snp.makeConstraints { make in
            make.edges.equalToSuperview().inset(UIEdgeInsets(top: topViewHeight, left: 0, bottom: 0, right: 0))
        }

        let logoutHeight = CGFloat(40)
        let margin = CGFloat(14)
        let containerHeight = CGFloat(logoutHeight + 2*margin)
        let bottomContainer = UIView(frame: .init(origin: .zero, size: .init(width: 400, height: containerHeight)))
        bottomContainer.backgroundColor = .classroomChildBG
        tableView.contentInset = .init(top: 0, left: 0, bottom: containerHeight, right: 0)
        bottomContainer.addSubview(logoutButton)
        view.addSubview(bottomContainer)
        bottomContainer.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.bottom.equalToSuperview()
            make.size.equalTo(CGSize(width: 400, height: containerHeight))
        }
        logoutButton.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.height.equalTo(logoutHeight)
            make.top.equalToSuperview().inset(margin)
        }
    }

    func config(cell: ClassRoomSettingTableViewCell, type: SettingControlType, hideBorder: Bool) {
        cell.label.text = type.description
        cell.selectionStyle = .none
        cell.switch.isHidden = false
        cell.rightArrowImageView.isHidden = true
        cell.cameraToggleView.isHidden = true
        cell.borderView.isHidden = hideBorder
        switch type {
        case .mirror:
            cell.switch.isHidden = false
            cell.iconView.image = nil
            cell.switch.isOn = frontMirror
            cell.switchValueChangedHandler = { [weak self] isOn in
                self?.frontMirror = isOn
            }
        case .cameraDirection:
            cell.switch.isHidden = true
            cell.iconView.image = nil
            cell.cameraToggleView.isHidden = false
            cell.cameraToggleView.selectedSegmentIndex = isCameraFront ? 0 : 1
            cell.cameraFaceFrontChangedHandler = { [weak self] _ in
                self?.isCameraFront.toggle()
            }
        case .shortcut:
            cell.switch.isHidden = true
            cell.rightArrowImageView.isHidden = false
            cell.setEnable(true)
            cell.iconView.image = UIImage.fromPlugin(named: "command")?.tintColor(.color(type: .text))
        case .camera:
            if cell.switch.isOn != cameraOn.value {
                cell.switch.isOn = cameraOn.value
            }
            cell.iconView.image = UIImage.fromPlugin(named: "camera")?.tintColor(.color(type: .text))
            cell.setEnable(deviceUpdateEnable.value)
            cell.switchValueChangedHandler = { [weak self] _ in
                self?.cameraPublish.accept(())
            }
        case .mic:
            if cell.switch.isOn != micOn.value {
                cell.switch.isOn = micOn.value
            }
            cell.setEnable(deviceUpdateEnable.value)
            cell.switchValueChangedHandler = { [weak self] _ in
                self?.micPublish.accept(())
            }
            cell.iconView.image = UIImage.fromPlugin(named: "microphone")?.tintColor(.color(type: .text))
        case .videoArea:
            cell.setEnable(true)
            cell.switch.isOn = videoAreaOn.value
            cell.switchValueChangedHandler = { [weak self] _ in
                self?.videoAreaPublish.accept(())
            }
            cell.iconView.image = UIImage.fromPlugin(named: "video_area")?.tintColor(.color(type: .text))
        }
    }

    // MARK: - Lazy

    lazy var logoutButton: UIButton = {
        let button = UIButton(type: .custom)
        button.backgroundColor = .classroomChildBG
        button.titleLabel?.font = .systemFont(ofSize: 16)
        if #available(iOS 13.0, *) {
            button.setTraitRelatedBlock { button in
                let color = UIColor.color(light: .red6, dark: .red5)
                let resolvedColor = color.resolvedColor(with: button.traitCollection)
                
                button.setTitleColor(resolvedColor, for: .normal)
                button.setImage(UIImage.fromPlugin(named: "logout")?.tintColor(resolvedColor), for: .normal)
                button.layer.borderColor = resolvedColor.cgColor
            }
        } else {
            // Fallback for iOS 12 (no traitCollection or dynamic colors)
            let color = UIColor.color(light: .red6, dark: .red5)
            
            button.setTitleColor(color, for: .normal)
            button.setImage(UIImage.fromPlugin(named: "logout")?.tintColor(color), for: .normal)
            button.layer.borderColor = color.cgColor
        }
        button.adjustsImageWhenHighlighted = false
        button.layer.borderWidth = commonBorderWidth
        button.layer.cornerRadius = 4
        button.layer.masksToBounds = true
        button.contentEdgeInsets = .init(top: 0, left: 20, bottom: 0, right: 20)
        button.setTitle(localizeStrings("Leaving Classroom"), for: .normal)
        return button
    }()

    lazy var topView: UIView = {
        let view = UIView(frame: .zero)
        view.backgroundColor = .classroomChildBG

        let leftIcon = UIImageView()
        if #available(iOS 13.0, *) {
            view.setTraitRelatedBlock { [weak leftIcon] v in
                let tintColor = UIColor.color(type: .text, .strong).resolvedColor(with: v.traitCollection)
                leftIcon?.image = UIImage.fromPlugin(named: "classroom_setting")?.tintColor(tintColor)
            }
        } else {
            // Fallback for iOS 12 (no traitCollection or dynamic colors)
            let tintColor = UIColor.color(type: .text, .strong)
            leftIcon.image = UIImage.fromPlugin(named: "classroom_setting")?.tintColor(tintColor)
        }

        leftIcon.contentMode = .scaleAspectFit
        view.addSubview(leftIcon)
        leftIcon.snp.makeConstraints { make in
            make.width.height.equalTo(24)
            make.centerY.equalToSuperview()
            make.left.equalToSuperview().inset(8)
        }

        let topLabel = UILabel(frame: .zero)
        topLabel.text = localizeStrings("Setting")
        topLabel.textColor = .color(type: .text, .strong)
        topLabel.font = .systemFont(ofSize: 14, weight: .semibold)
        view.addSubview(topLabel)
        topLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalToSuperview().inset(40)
        }
        view.addLine(direction: .bottom, color: .borderColor)
        return view
    }()

    lazy var tableView: UITableView = {
        let view = UITableView(frame: .zero, style: .plain)
        view.backgroundColor = .classroomChildBG
        view.contentInsetAdjustmentBehavior = .never
        view.separatorStyle = .none
        view.register(.init(nibName: String(describing: ClassRoomSettingTableViewCell.self), bundle: AgoraNativePlugin.pluginBundle), forCellReuseIdentifier: cellIdentifier)
        view.delegate = self
        view.dataSource = self
        view.rowHeight = 46
        view.showsVerticalScrollIndicator = false
        return view
    }()
}

extension ClassRoomSettingViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as! ClassRoomSettingTableViewCell
        let type = models[indexPath.section][indexPath.row]
        config(cell: cell, type: type, hideBorder: indexPath.row != models[indexPath.section].count - 1)
        return cell
    }

    func numberOfSections(in tableView: UITableView) -> Int {
        models.count
    }
    func tableView(_: UITableView, numberOfRowsInSection section: Int) -> Int {
        models[section].count
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if models[indexPath.section][indexPath.row] == .shortcut {
            preferencePublish.accept(())
        }
        tableView.deselectRow(at: indexPath, animated: true)
    }
}
