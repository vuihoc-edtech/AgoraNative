#
# To learn more about a s.dependencyspec see http://guides.cocoas.dependencys.org/syntax/s.dependencyspec.html.
# Run `s.dependency lib lint agora_native.s.dependencyspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'vh_agora_native'
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
  'agora_native' => ['Assets/*.xcassets', 'Assets/*.json', 'Assets/*.mp3', 'Assets/*.lproj']
  }
  s.dependency 'Flutter'
  s.dependency 'AgoraRtm_iOS', '2.2.4'
  s.dependency 'AgoraRtcEngine_iOS/RtcBasic', '4.5.2'
  s.dependency 'Fastboard', '2.0.0-alpha.19'
  s.dependency 'Whiteboard', '2.17.0-alpha.30'
  s.dependency 'RxSwift', '6.9.0'
  s.dependency 'RxCocoa', '6.9.0'
  s.dependency 'NSObject+Rx', '5.2.2'
  s.dependency 'RxDataSources', '5.0.0'
  s.dependency 'CropViewController', '2.7.4'
  s.dependency 'Zip', '2.1.2'
  s.dependency 'lottie-ios', '4.4.1'
  s.dependency 'ScreenCorners', '1.0.1'
  s.dependency 'ViewDragger', '1.1.0'
  s.dependency 'MBProgressHUD', '~> 1.2.0'
  s.dependency 'Kingfisher', '7.12.0'
  s.dependency 'Hero', '1.6.4'
  s.dependency 'SnapKit', '5.7.1'
  s.dependency 'DZNEmptyDataSet', '1.8.1'

  s.platform = :ios, '12.1'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'

  # If your plugin requires a privacy manifest, for example if it uses any
  # required reason APIs, update the PrivacyInfo.xcprivacy file to describe your
  # plugin's privacy impact, and then uncomment this line. For more information,
  # see https://developer.apple.com/documentation/bundleresources/privacy_manifest_files
  # s.resource_bundles = {'agora_native_privacy' => ['Resources/PrivacyInfo.xcprivacy']}
end
