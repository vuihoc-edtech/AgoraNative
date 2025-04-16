//
//  HomeTabbar.swift
//  Flat
//
//  Created by xuyunshi on 2021/10/18.
//  Copyright © 2021 agora.io. All rights reserved.
//

import UIKit

class HomeTabBar: UITabBar {
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupAppearance()
    }

    override func traitCollectionDidChange(_: UITraitCollection?) {
        setupAppearance()
    }

    @available(*, unavailable)
    required init?(coder _: NSCoder) {
        fatalError()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        items?.forEach { item in
            if let title = item.title, let attribute = item.titleTextAttributes(for: .normal) {
                let textHeight = NSString(string: title).size(withAttributes: attribute).height
                item.titlePositionAdjustment = .init(horizontal: 0, vertical: -((bounds.height - textHeight) / 2))
            }
        }
    }

    func setupAppearance() {
        tintColor = .color(type: .text)
        unselectedItemTintColor = .color(type: .text)
        backgroundColor = .color(type: .background)
        backgroundImage = .imageWith(color: .color(type: .background))
        shadowImage = .imageWith(color: .borderColor)
        selectionIndicatorImage = createSelectionIndicator(color: .color(type: .primary),
                                                           size: .init(width: 44, height: 44),
                                                           lineHeight: 2)
    }

    func createSelectionIndicator(color: UIColor, size: CGSize, lineHeight: CGFloat) -> UIImage {
        UIGraphicsBeginImageContext(size)
        color.setFill()
        UIRectFill(.init(x: 0, y: size.height - lineHeight, width: size.width, height: lineHeight))
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return image!
    }

    func addItem(title: String, tag: Int) -> UITabBarItem {
        let item = UITabBarItem(title: title, image: nil, tag: tag)
        item.setTitleTextAttributes([.font: UIFont.systemFont(ofSize: 14, weight: .medium)], for: .normal)
        return item
    }
}
