//
//  WhiteboardAppsViewController.swift
//  Flat
//
//  Created by xuyunshi on 2022/9/5.
//  Copyright © 2022 agora.io. All rights reserved.
//

import UIKit
import Whiteboard

class WhiteboardAppsViewController: UIViewController {
    struct WhiteboardAppItem {
        let title: String
        let imageName: String
        let appParams: WhiteAppParam?
    }

    let appItems = [
        "Monaco",
        "GeoGebra",
        "Countdown",
        "Selector",
        "Dice",
        "MindMap",
        "Quill",
    ].map {
        WhiteboardAppItem(title: localizeStrings($0), imageName: "apps_" + $0, appParams: .init(kind: $0, options: .init(), attrs: [:]))
    }

    lazy var items: [WhiteboardAppItem] = {
        var i = appItems
        i.append(WhiteboardAppItem(title: localizeStrings("whiteboard_save_annotation"), imageName: "save_whiteboard", appParams: nil))
        return i
    }()

    var room: WhiteRoom?
    weak var clickSource: UIButton?

    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        let rows = ceil(CGFloat(items.count) / numberPerRow)
        let margins = layout.minimumLineSpacing * (rows - 1)
        preferredContentSize = .init(width: layout.itemSize.width * numberPerRow + layout.sectionInset.left + layout.sectionInset.right,
                                     height: layout.itemSize.height * rows + layout.sectionInset.top + layout.sectionInset.bottom + margins)
    }

    @available(*, unavailable)
    required init?(coder _: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupViews()
    }

    // MARK: - Private

    func setupViews() {
        view.backgroundColor = .classroomChildBG
        view.addSubview(collectionView)
        collectionView.snp.makeConstraints { make in
            make.edges.equalTo(view.safeAreaLayoutGuide)
        }
    }

    // MARK: - Lazy

    let numberPerRow: CGFloat = 3

    lazy var layout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = .init(width: 120, height: 66 + 8)
        layout.minimumLineSpacing = 14
        layout.minimumInteritemSpacing = 0
        layout.sectionInset = .init(inset: 16)
        return layout
    }()

    lazy var collectionView: UICollectionView = {
        let view = UICollectionView(frame: .zero, collectionViewLayout: layout)
        view.backgroundColor = .classroomChildBG
        view.register(WhiteboardAppsCell.self, forCellWithReuseIdentifier: String(describing: WhiteboardAppsCell.self))
        view.delegate = self
        view.dataSource = self
        return view
    }()
}

extension WhiteboardAppsViewController: UICollectionViewDelegate, UICollectionViewDataSource {
    func numberOfSections(in _: UICollectionView) -> Int { 1 }
    func collectionView(_: UICollectionView, numberOfItemsInSection _: Int) -> Int { items.count }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String(describing: WhiteboardAppsCell.self), for: indexPath) as! WhiteboardAppsCell
        let item = items[indexPath.row]
        cell.appTitleLabel.text = item.title
        cell.appIconView.image = UIImage.fromPlugin(named: item.imageName)
        return cell
    }

    func collectionView(_: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        guard let room, let parent = presentingViewController, let source = clickSource else { return }

        let item = items[indexPath.row]
        dismiss(animated: true) {
            if let params = item.appParams {
                room.addApp(params) { _ in }
                return
            }
            let vc = WhiteboardScenesListViewController(room: room)
            parent.popoverViewController(viewController: vc, fromSource: source)
        }
    }
}
