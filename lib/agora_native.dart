import 'agora_native_platform_interface.dart';

class AgoraNative {
  Future<String?> getPlatformVersion() {
    return AgoraNativePlatform.instance.getPlatformVersion();
  }

  void joinClassRoom(String roomId, String userName, UserRole role) {
    AgoraNativePlatform.instance.joinClassRoom(roomId, userName, role.value);
  }
}

enum UserRole {
  student(2),
  teacher(1),
  audience(4);

  final int value;
  const UserRole(this.value);
}
