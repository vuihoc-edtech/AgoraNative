import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:vh_agora_native/agora_native.dart';
import 'agora_native_platform_interface.dart';
import 'package:uuid/uuid.dart';

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
  Future<int> joinClassRoom(String roomUUID, bool cam, bool mic) async {
    try {
      final res = await methodChannel.invokeMethod<int>(
        "joinClassRoom",
        {'roomID': roomUUID, 'cam': cam, 'mic': mic},
      );
      return res ?? -2;
    } catch (e, st) {
      log('joinClassRoom error: ${st.toString()}');
      return -2;
    }
  }

  @override
  Future<bool> saveLoginInfo(Map<String, dynamic> user) async {
    try {
      final res = await methodChannel.invokeMethod<bool>("saveLoginInfo", user);
      return res ?? false;
    } catch (e, st) {
      log('saveLoginInfo error: ${st.toString()}');
      return false;
    }
  }

  @override
  Future<String> getGlobalUUID() async {
    try {
      final res = await methodChannel.invokeMethod<String>("getGlobalUUID");
      return res ?? const Uuid().v4();
    } catch (e, st) {
      log('getGlobalUUID error: ${st.toString()}');
      return const Uuid().v4();
    }
  }

  @override
  Future<bool> saveConfigs(Map<String, dynamic> configs) async {
    try {
      configs.addAll({'baseUrl': AgoraNative.baseUrl});
      final res =
          await methodChannel.invokeMethod<bool>("saveConfigs", configs);
      return res ?? false;
    } catch (e, st) {
      log('saveConfigs error: ${st.toString()}');
      return false;
    }
  }

  @override
  Future<void> setBotUsers(List<String> users) async {
    try {
      await methodChannel.invokeMethod("setBotUsers", users);
    } catch (e, st) {
      log('setBotUsers error: ${st.toString()}');
    }
  }

  @override
  Future<void> setWhiteBoardBackground(Color color) async {
    try {
      await methodChannel.invokeMethod('setWhiteBoardBackground', color.value);
    } catch (e, st) {
      log('setWhiteBoardBackground: ${st.toString()}');
    }
  }

  @override
  Future<void> postLogin() async {
    try {
      await methodChannel.invokeMethod('postLogin');
    } catch (e, st) {
      log('postLogin: ${st.toString()}');
    }
  }
}
