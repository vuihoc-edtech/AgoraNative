//
//  StringLocalize.swift
//  Flat
//
//  Created by xuyunshi on 2022/5/18.
//  Copyright © 2022 agora.io. All rights reserved.
//

import Foundation

func localizeStrings(_ strs: String...) -> String {
    strs.reduce(into: "") { partialResult, str in
        if partialResult.isEmpty {
            return partialResult += NSLocalizedString(str, bundle: AgoraNativePlugin.resourceBundle, comment: "")
        } else {
            return partialResult += (" " + NSLocalizedString(str, bundle: AgoraNativePlugin.resourceBundle, comment: ""))
        }
    
    }
}
