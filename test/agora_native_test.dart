import 'package:flutter_test/flutter_test.dart';
import 'package:agora_native/agora_native.dart';
import 'package:agora_native/agora_native_platform_interface.dart';
import 'package:agora_native/agora_native_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockAgoraNativePlatform
    with MockPlatformInterfaceMixin
    implements AgoraNativePlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
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
