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
    try {
      if (res["status"] == 0) {
        final user = User.fromJson(res["data"]);
        final saved =
            await AgoraNativePlatform.instance.saveLoginInfo(user.toJson());
        if (user.token != null) {
          final configs = await Auth.shared.getCofigs(user.token!);
          if (configs['data'] != null) {
            final data = configs['data'] as Map<String, dynamic>;
            data.addAll({'baseUrl': Auth.baseUrl});
            await AgoraNativePlatform.instance.saveConfigs(data);
          }
        }
        return saved;
      }
    } catch (e, st) {
      log(e.toString(), stackTrace: st);
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

  static String get baseUrl => Auth.baseUrl;

  static set baseUrl(String url) {
    Auth.baseUrl = url;
  }
}
