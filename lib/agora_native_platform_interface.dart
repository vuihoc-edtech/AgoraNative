import 'dart:ui';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'agora_native_method_channel.dart';

abstract class AgoraNativePlatform extends PlatformInterface {
  /// Constructs a AgoraNativePlatform.
  AgoraNativePlatform() : super(token: _token);

  static final Object _token = Object();

  static AgoraNativePlatform _instance = MethodChannelAgoraNative();

  /// The default instance of [AgoraNativePlatform] to use.
  ///
  /// Defaults to [MethodChannelAgoraNative].
  static AgoraNativePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [AgoraNativePlatform] when
  /// they register themselves.
  static set instance(AgoraNativePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('getPlatformVersion() has not been implemented.');
  }

  Future<int> joinClassRoom(String roomUUID, bool cam, bool mic) {
    throw UnimplementedError('joinClassRoom() has not been implemented.');
  }

  Future<bool> saveLoginInfo(Map<String, dynamic> user) {
    throw UnimplementedError('saveLoginInfo() has not been implemented.');
  }

  Future<String> getGlobalUUID() {
    throw UnimplementedError('getGlobalUUID() has not been implemented.');
  }

  Future<bool> saveConfigs(Map<String, dynamic> configs) {
    throw UnimplementedError('saveConfigs() has not been implemented.');
  }

  Future<void> setBotUsers(List<String> users) {
    throw UnimplementedError('saveBotUsers() has not been implemented.');
  }

  Future<void> setWhiteBoardBackground(Color color) {
    throw UnimplementedError('saveBotUsers() has not been implemented.');
  }
}
