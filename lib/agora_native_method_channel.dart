import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'agora_native_platform_interface.dart';

/// An implementation of [AgoraNativePlatform] that uses method channels.
class MethodChannelAgoraNative extends AgoraNativePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('agora_native');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
