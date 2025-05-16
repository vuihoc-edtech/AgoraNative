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
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool> joinClassRoom(String roomUUID) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool> saveLoginInfo(Map<String, dynamic> user) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String> getGlobalUUID() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Map<String, String> getEnv() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
