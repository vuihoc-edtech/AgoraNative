import 'dart:ui';

import 'package:vh_agora_native/user_agora.dart';

import 'agora_native_platform_interface.dart';

class AgoraNative {
  static String baseUrl = 'dev-class-api.rinoedu.ai';

  Future<String?> getPlatformVersion() {
    return AgoraNativePlatform.instance.getPlatformVersion();
  }

  //Step 1
  Future<bool> saveLoginInfo(UserAgora user) {
    return AgoraNativePlatform.instance.saveLoginInfo(user.toJson());
  }

  //Step 2
  Future<bool> saveConfigs(Map<String, dynamic> configs) {
    return AgoraNativePlatform.instance.saveConfigs(configs);
  }

  //Step 3
  Future<void> setBotUsers(List<String> users) async {
    await AgoraNativePlatform.instance.setBotUsers(users);
  }

  //Step 4
  Future<void> setWhiteboardBackground(Color color) async {
    await AgoraNativePlatform.instance.setWhiteBoardBackground(color);
  }

  //Step 5
  Future<int> joinClassRoom(String roomUUID) {
    return AgoraNativePlatform.instance.joinClassRoom(roomUUID);
  }
}
