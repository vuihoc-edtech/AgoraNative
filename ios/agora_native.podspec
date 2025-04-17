#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint agora_native.podspec` to validate before publishing.
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
  s.dependency 'Flutter'
  # s.dependency 'CocoaLumberjack',   '3.6.1'
  # s.dependency 'AliyunOSSiOS',      '2.10.8'
  # s.dependency 'SSZipArchive',      '2.4.2'
  # s.dependency 'SwifterSwift',      '5.2.0'
  # s.dependency 'SDWebImage',        '5.12.0'
  # s.dependency 'Masonry',           '1.1.0'
  # s.dependency 'Armin',             '1.1.1'
  
  # # agora libs
  # s.dependency 'AgoraRtcEngine_iOS/RtcBasic', '3.7.2'
  # s.dependency 'AgoraMediaPlayer_iOS',        '1.3.0'
  # s.dependency 'Agora_Chat_iOS',              '1.0.6'
  # s.dependency 'AgoraRtm_iOS',                '1.5.1'
  # s.dependency 'Whiteboard',                  '2.16.51'
 
  # # open source libs
  # s.dependency 'AgoraClassroomSDK_iOS', '2.8.101'
  # s.dependency 'AgoraEduUI',            '2.8.101'
  
  # s.dependency 'AgoraProctorSDK',       '1.0.1'
  # s.dependency 'AgoraProctorUI',        '1.0.0'
  
  # s.dependency 'AgoraWidgets',          '2.8.101'
  
  # # close source libs
  # s.dependency 'AgoraUIBaseViews',      '2.8.101'
  # s.dependency 'AgoraEduCore', 	       '2.8.101'
  # s.dependency 'AgoraWidget',           '2.8.0'

  s.dependency 'SwifterSwift',      '5.2.0'
  s.dependency 'Masonry',           '1.1.0'
  s.dependency 'SDWebImage',        '5.12.0'
  
  s.dependency 'AgoraRtcEngine_Special_iOS', '3.7.2.133'
  s.dependency 'Whiteboard', '2.16.102'
 
  # agora libs header
  s.dependency 'AgoraClassroomSDK_iOS', '2.8.105'
  s.dependency 'AgoraProctorSDK',       '1.0.2'
  s.dependency 'AgoraWidgets',          '2.8.105'

  s.platform = :ios, '12.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'

  # If your plugin requires a privacy manifest, for example if it uses any
  # required reason APIs, update the PrivacyInfo.xcprivacy file to describe your
  # plugin's privacy impact, and then uncomment this line. For more information,
  # see https://developer.apple.com/documentation/bundleresources/privacy_manifest_files
  # s.resource_bundles = {'agora_native_privacy' => ['Resources/PrivacyInfo.xcprivacy']}
end
