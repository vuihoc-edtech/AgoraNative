#
# To learn more about a s.dependencyspec see http://guides.cocoas.dependencys.org/syntax/s.dependencyspec.html.
# Run `s.dependency lib lint agora_native.s.dependencyspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'agora_native'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter Agora project.'
  s.description      = <<-DESC
A new Flutter Agora project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.resource_bundles = {
  'agora_native' => ['Assets/*.xcassets']
  }
  s.dependency 'Flutter'
  s.dependency 'Agora_Chat_iOS', "~> 1.0.6"
  s.dependency 'AgoraRtm_iOS', '2.2.4'
  s.dependency 'AgoraRtcEngine_iOS', '4.3.0'
  s.dependency 'Fastboard', '2.0.0-alpha.19'
  s.dependency 'Whiteboard', '2.17.0-alpha.30'
  s.dependency 'Whiteboard/SyncPlayer', '2.17.0-alpha.30'
  s.dependency 'RxSwift', '6.9.0'
  s.dependency 'RxCocoa', '6.9.0'
  s.dependency 'NSObject+Rx', '5.2.2'
  s.dependency 'RxDataSources', '5.0.0'
  s.dependency 'AcknowList', '3.3.0'
  s.dependency 'CropViewController', '2.7.4'
  s.dependency 'Siren', '6.1.3'
  s.dependency 'IQKeyboardManagerSwift', '8.0.1'
  s.dependency 'Zip', '2.1.2'
  s.dependency 'lottie-ios', '4.5.1'
  s.dependency 'PhoneNumberKit', '3.7.10'
  s.dependency 'ScreenCorners', '1.0.1'

  s.dependency 'SyncPlayer', '0.3.3'
  s.dependency 'ViewDragger', '1.1.0'
  
  s.dependency 'MBProgressHUD', '~> 1.2.0'
  s.dependency 'Kingfisher', '8.3.2'
  s.dependency 'Hero', '1.6.4'
  s.dependency 'SnapKit', '5.7.1'
  s.dependency 'DZNEmptyDataSet', '1.8.1'
  s.dependency 'Logging', '1.4.0'
  s.dependency 'SwiftyBeaver', '1.9.5'
  s.dependency 'AliyunLogProducer/Core'
  s.dependency 'AliyunLogProducer/Bricks'
  s.platform = :ios, '13'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'

  # If your plugin requires a privacy manifest, for example if it uses any
  # required reason APIs, update the PrivacyInfo.xcprivacy file to describe your
  # plugin's privacy impact, and then uncomment this line. For more information,
  # see https://developer.apple.com/documentation/bundleresources/privacy_manifest_files
  # s.resource_bundles = {'agora_native_privacy' => ['Resources/PrivacyInfo.xcprivacy']}
end
