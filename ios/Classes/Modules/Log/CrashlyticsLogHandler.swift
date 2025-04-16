//
//  CrashlyticsLogHandler.swift
//  Flat
//
//  Created by xuyunshi on 2022/11/28.
//  Copyright © 2022 agora.io. All rights reserved.
//

import Logging

struct CrashlyticsLogHandler: LogHandler {
    subscript(metadataKey key: String) -> Logging.Logger.Metadata.Value? {
        get {
            metadata[key]
        }
        set(newValue) {
            metadata[key] = newValue
        }
    }

    var metadata: Logging.Logger.Metadata = [:]

    var logLevel: Logging.Logger.Level = .info

    func log(level _: Logger.Level,
             message: Logger.Message,
             metadata _: Logger.Metadata?,
             source: String,
             file: String,
             function: String,
             line: UInt)
    {
        let msg = "\(message)".replacingOccurrences(of: ", ", with: " ").replacingOccurrences(of: "\n", with: "")
        let formattedMsg = "\(source.isEmpty ? "" : "[\(source)],") \(msg)"
    }
}
