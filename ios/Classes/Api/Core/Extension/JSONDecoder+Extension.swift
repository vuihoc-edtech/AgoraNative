//
//  Decoder+Extension.swift
//  Flat
//
//  Created by xuyunshi on 2021/10/22.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Foundation

private let defaultFlatDecoder: JSONDecoder = {
    let decoder = JSONDecoder()
    decoder.dateDecodingStrategy = .millisecondsSince1970
    return decoder
}()

private let defaultAgoraDecoder: JSONDecoder = {
    let decoder = JSONDecoder()
    decoder.dateDecodingStrategy = .iso8601
    return decoder
}()

extension JSONDecoder {
    static var flatDecoder: JSONDecoder {
        defaultFlatDecoder
    }

    static var agoraDecoder: JSONDecoder {
        defaultAgoraDecoder
    }
}
