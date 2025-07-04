//
//  Utils.swift
//  vh_agora_native
//
//  Created by Kien Nguyen on 3/7/25.
//

import AVFoundation
import AVFAudio

class Utils {
    static func isGrantedCamera() -> Bool {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        return status == .authorized
    }
    
    static func isGrantedMicrophone() -> Bool {
        let status = AVAudioSession.sharedInstance().recordPermission
        return status == .granted
    }
}
