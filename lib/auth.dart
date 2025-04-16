import 'package:agora_native/agora_native_platform_interface.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:uuid/uuid.dart';

class Auth {
  Auth._();
  static final _instance = Auth._();
  static Auth shared = _instance;
  Future<Map<String, dynamic>> loginWithEmail(
      String email, String password) async {
    // Generate UUIDs for headers
    const uuid = Uuid();
    final requestId = uuid.v4();
    String sessionId = await AgoraNativePlatform.instance.getGlobalUUID();
    if (sessionId.isEmpty) {
      sessionId = uuid.v4();
    }
    final response = await http.post(
      Uri.parse('https://api.flat.agora.io/v2/login/email'),
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
        'x-request-id': requestId,
        'x-session-id': sessionId,
      },
      body: jsonEncode({
        'email': email,
        'password': password,
      }),
    );

    if (response.statusCode == 200) {
      // Handle successful login
      final responseData = jsonDecode(response.body);
      return responseData as Map<String, dynamic>;
    } else {
      // Handle error
      return {};
    }
  }
}
