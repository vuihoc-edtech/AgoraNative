//
//  Rtc+audioMixer.swift
//  Flat
//
//  Created by xuyunshi on 2022/12/2.
//  Copyright © 2022 agora.io. All rights reserved.
//

import AgoraRtcKit
import Fastboard
import Foundation
import Whiteboard

private let agoraSuccessStateCode = 0

private let agoraPlayErrorStateCode = 714

// Just a placeholder. Means nothing
private let agoraPlaceholderStateCode = 0

extension Rtc: FastAudioMixerDelegate {
    func startAudioMixing(audioBridge: WhiteAudioMixerBridge, filePath: String, loopback: Bool, replace _: Bool, cycle: Int) {
        currentAudioBridge = audioBridge
        let errorCode = Int(agoraKit.startAudioMixing(filePath, loopback: loopback, cycle: cycle, startPos: 0))
        print("mixing start code: \(errorCode)")
        if errorCode != agoraSuccessStateCode {
            audioBridge.setMediaState(agoraPlayErrorStateCode, errorCode: errorCode)
        }
    }

    func stopAudioMixing(audioBridge: WhiteAudioMixerBridge) {
        let errorCode = Int(agoraKit.stopAudioMixing())
        print("mixing stop code: \(errorCode)")
        if errorCode != agoraSuccessStateCode {
            audioBridge.setMediaState(agoraPlaceholderStateCode, errorCode: errorCode)
        }
    }

    func pauseAudioMixing(audioBridge: WhiteAudioMixerBridge) {
        let errorCode = Int(agoraKit.pauseAudioMixing())
        print("mixing pause code: \(errorCode)")
        if errorCode != agoraSuccessStateCode {
            audioBridge.setMediaState(agoraPlaceholderStateCode, errorCode: errorCode)
        }
    }

    func resumeAudioMixing(audioBridge: WhiteAudioMixerBridge) {
        let errorCode = Int(agoraKit.resumeAudioMixing())
        print("mixing resume code: \(errorCode)")
        if errorCode != agoraSuccessStateCode {
            audioBridge.setMediaState(agoraPlaceholderStateCode, errorCode: errorCode)
        }
    }

    func setAudioMixingPosition(audioBridge: WhiteAudioMixerBridge, _ position: Int) {
        let errorCode = Int(agoraKit.setAudioMixingPosition(position))
        print("mixing set position: \(position), code: \(errorCode)")
        if errorCode != agoraSuccessStateCode {
            audioBridge.setMediaState(agoraPlaceholderStateCode, errorCode: errorCode)
        }
    }
}

private var currentAudioBridgeKey: Void?
extension Rtc {
    weak var currentAudioBridge: WhiteAudioMixerBridge? {
        get {
            objc_getAssociatedObject(self, &currentAudioBridgeKey) as? WhiteAudioMixerBridge
        }
        set {
            objc_setAssociatedObject(self, &currentAudioBridgeKey, newValue, .OBJC_ASSOCIATION_ASSIGN)
        }
    }

    func rtcEngine(_: AgoraRtcEngineKit, audioMixingStateChanged state: AgoraAudioMixingStateType, reasonCode: AgoraAudioMixingReasonCode) {
        print("mixing rtc state update state: \(state.rawValue), errorCode: \(reasonCode.rawValue) ")
        currentAudioBridge?.setMediaState(state.rawValue, errorCode: reasonCode.rawValue)
    }
}
