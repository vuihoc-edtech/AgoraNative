import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:vh_agora_native/auth.dart';

import 'agora_native_platform_interface.dart';

/// An implementation of [AgoraNativePlatform] that uses method channels.
class MethodChannelAgoraNative extends AgoraNativePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('agora_native');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> joinClassRoom(String roomUUID) async {
    return await methodChannel.invokeMethod("joinClassRoom", roomUUID);
  }

  @override
  Future<bool> saveLoginInfo(Map<String, dynamic> user) async {
    final res = await methodChannel.invokeMethod<bool>("saveLoginInfo", user);
    return res ?? false;
  }

  @override
  Future<String> getGlobalUUID() async {
    final res = await methodChannel.invokeMethod<String>("getGlobalUUID");
    return res ?? '';
  }

  @override
  Future<bool> saveConfigs(Map<String, dynamic> configs) async {
    final res = await methodChannel.invokeMethod<bool>("saveConfigs", configs);
    return res ?? false;
  }
}
