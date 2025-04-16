import 'dart:developer';

import 'package:agora_native/auth.dart';
import 'package:agora_native/user.dart';

import 'agora_native_platform_interface.dart';

class AgoraNative {
  Future<String?> getPlatformVersion() {
    return AgoraNativePlatform.instance.getPlatformVersion();
  }

  void joinClassRoom(String roomUUID) {
    AgoraNativePlatform.instance.joinClassRoom(roomUUID);
  }

  Future<bool> login() async {
    final res = await Auth.shared
        .loginWithEmail("trungkien71297@gmail.com", "trungkien123");
    if (res["status"] == 0) {
      final user = User.fromJson(res["data"]);
      final saved = AgoraNativePlatform.instance.saveLoginInfo(user.toJson());
      return saved;
    }
    return false;
  }
}
