import 'dart:ui';

import 'package:flutter_test/flutter_test.dart';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:vh_agora_native/agora_native.dart';
import 'package:vh_agora_native/agora_native_method_channel.dart';
import 'package:vh_agora_native/agora_native_platform_interface.dart';

class MockAgoraNativePlatform
    with MockPlatformInterfaceMixin
    implements AgoraNativePlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<String> getGlobalUUID() {
    // TODO: implement getGlobalUUID
    throw UnimplementedError();
  }

  @override
  Future<bool> setBotUsers(List<String> users) {
    // TODO: implement saveBotUsers
    throw UnimplementedError();
  }

  @override
  Future<bool> saveConfigs(Map<String, dynamic> configs) {
    // TODO: implement saveConfigs
    throw UnimplementedError();
  }

  @override
  Future<bool> saveLoginInfo(Map<String, dynamic> user) {
    // TODO: implement saveLoginInfo
    throw UnimplementedError();
  }

  @override
  Future<void> setWhiteBoardBackground(Color color) {
    // TODO: implement setWhiteBoardBackground
    throw UnimplementedError();
  }

  @override
  Future<int> joinClassRoom(String roomUUID, bool cam, bool mic) {
    // TODO: implement joinClassRoom
    throw UnimplementedError();
  }
}

void main() {
  final AgoraNativePlatform initialPlatform = AgoraNativePlatform.instance;

  test('$MethodChannelAgoraNative is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelAgoraNative>());
  });

  test('getPlatformVersion', () async {
    AgoraNative agoraNativePlugin = AgoraNative();
    MockAgoraNativePlatform fakePlatform = MockAgoraNativePlatform();
    AgoraNativePlatform.instance = fakePlatform;

    expect(await agoraNativePlugin.getPlatformVersion(), '42');
  });
}
