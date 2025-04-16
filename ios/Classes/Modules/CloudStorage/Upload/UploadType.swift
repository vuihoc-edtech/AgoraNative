//
//  UploadType.swift
//  Flat
//
//  Created by xuyunshi on 2021/12/13.
//  Copyright © 2021 agora.io. All rights reserved.
//

import UIKit
import UniformTypeIdentifiers

enum UploadType: String, CaseIterable {
    case image
    case video
    case audio
    case doc

    @available(iOS 14.0, *)
    var utTypes: [UTType] {
        switch self {
        case .image:
            return [.image]
        case .video:
            return [.mpeg4Movie]
        case .audio:
            return [.mp3]
        case .doc:
            let fileExtensions = ["doc", "docx", "pdf", "ppt", "pptx"]
            return fileExtensions.compactMap { UTType(filenameExtension: $0) }
        }
    }

    var allowedUTStrings: [String] {
        switch self {
        case .image:
            return ["public.image"]
        case .video:
            return ["public.movie"]
        case .audio:
            return ["public.mp3"]
        case .doc:
            return ["com.adobe.pdf",
                    "com.microsoft.word.doc",
                    "org.openxmlformats.wordprocessingml.document",
                    "com.microsoft.powerpoint.​ppt",
                    "org.openxmlformats.presentationml.presentation"]
        }
    }

    var title: String {
        let str = rawValue.first!.uppercased() + rawValue.dropFirst()
        return localizeStrings("Upload " + str)
    }

    var imageName: String { "upload_" + rawValue }

    var bgColor: UIColor {
        switch self {
        case .image:
            return .init(hexString: "#00A0FF")
        case .video:
            return .init(hexString: "#6B6ECF")
        case .audio:
            return .init(hexString: "#56C794")
        case .doc:
            return .init(hexString: "#3A69E5")
        }
    }
}
