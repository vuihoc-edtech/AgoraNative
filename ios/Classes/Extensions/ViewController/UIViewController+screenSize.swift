//
//  UIViewController+screenSize.swift
//  Flat
//
//  Created by xuyunshi on 2022/9/21.
//  Copyright © 2022 agora.io. All rights reserved.
//

import UIKit

extension UIViewController {
    private var mainWindowBounds: CGRect { UIScreen.main.bounds }

    var greatWindowSide: CGFloat {
        if #available(iOS 13.0, *) {
            // Use windowScene for iOS 13 and above
            guard let bounds = view.window?.windowScene?.screen.bounds else {
                let bounds = mainWindowBounds
                return max(bounds.width, bounds.height)
            }
            return max(bounds.width, bounds.height)
        } else {
            // Fallback for iOS 12.1
            let bounds = view.window?.bounds ?? mainWindowBounds
            return max(bounds.width, bounds.height)
        }
    }
    
    var smallerWindowSide: CGFloat {
        if #available(iOS 13.0, *) {
            // Use windowScene for iOS 13 and above
            guard let bounds = view.window?.windowScene?.screen.bounds else {
                let bounds = mainWindowBounds
                return min(bounds.width, bounds.height)
            }
            return min(bounds.width, bounds.height)
        } else {
            // Fallback for iOS 12.1
            let bounds = view.window?.bounds ?? mainWindowBounds
            return min(bounds.width, bounds.height)
        }
    }
}
