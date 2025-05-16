import 'dart:developer';

import 'package:vh_agora_native/auth.dart';
import 'package:vh_agora_native/user.dart';

import 'agora_native_platform_interface.dart';

class AgoraNative {
  Future<String?> getPlatformVersion() {
    return AgoraNativePlatform.instance.getPlatformVersion();
  }

  Future<bool> joinClassRoom(String roomUUID) {
    return AgoraNativePlatform.instance.joinClassRoom(roomUUID);
  }

  Future<bool> login() async {
    final res =
        await Auth.shared.loginWithEmail("ndql1996@gmail.com", "Abc123123");
    if (res["status"] == 0) {
      final user = User.fromJson(res["data"]);
      final saved = AgoraNativePlatform.instance.saveLoginInfo(user.toJson());
      return saved;
    }
    return false;
  }

  Future<bool> loginWithToken(String token) async {
    final res = await Auth.shared.loginCheck(token);
    if (res["status"] == 0) {
      final user = User.fromJson(res["data"]);
      final saved = AgoraNativePlatform.instance.saveLoginInfo(user.toJson());
      return saved;
    }
    return false;
  }

  Future<bool> loginWithTokenVH(String token) async {
    final res = await Auth.shared.loginVHToken(token);
    if (res["status"] == 0) {
      final user = User.fromJson(res["data"]);
      final saved = AgoraNativePlatform.instance.saveLoginInfo(user.toJson());
      return saved;
    }
    return false;
  }

  Future<bool> joinRoomWith(String token, String roomId) async {
    final res = await loginWithToken(token);
    if (!res) {
      return false;
    }
    try {
      final joined = joinClassRoom(roomId);
      return joined;
    } catch (e, st) {
      log(st.toString());
    }
    return false;
  }

  Future<bool> joinRoomVH(String token, String roomId) async {
    final res = await loginWithTokenVH(token);
    if (res) {
      try {
        final joined = joinClassRoom(roomId);
        return joined;
      } catch (e, st) {
        log(st.toString());
      }
    }
    return false;
  }
}
