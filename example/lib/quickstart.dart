import 'package:agora_native/agora_native.dart';
import 'package:flutter/material.dart';

class QuickJoinForm extends StatefulWidget {
  const QuickJoinForm({super.key});

  @override
  State<QuickJoinForm> createState() => _QuickJoinFormState();
}

class _QuickJoinFormState extends State<QuickJoinForm> {
  final _agoraNativePlugin = AgoraNative();

  String roomId = '716510943';
  String userName = 'Hoàng Dược Sư';
  UserRole? role = UserRole.student;
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Row(
        children: [
          Expanded(
            flex: 3,
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text("Room ID",
                      style: TextStyle(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 6),
                  TextFormField(
                    initialValue: roomId,
                    decoration: _inputDecoration(),
                    onChanged: (value) {
                      roomId = value;
                    },
                  ),
                  const SizedBox(height: 20),
                  const Text("Name",
                      style: TextStyle(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 6),
                  TextFormField(
                    initialValue: userName,
                    decoration: _inputDecoration(),
                    onChanged: (value) {
                      userName = value;
                    },
                  ),
                  const SizedBox(height: 20),
                  const Text("Role",
                      style: TextStyle(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 6),
                  DropdownButtonFormField<UserRole>(
                    value: role,
                    items: const [
                      DropdownMenuItem(
                          value: UserRole.student, child: Text("Student")),
                      DropdownMenuItem(
                          value: UserRole.teacher, child: Text("Teacher")),
                      DropdownMenuItem(
                          value: UserRole.audience, child: Text("Audience")),
                    ],
                    onChanged: (r) {
                      setState(() {
                        role = r;
                      });
                    },
                    decoration: _inputDecoration(),
                  ),
                  const SizedBox(height: 30),
                  SizedBox(
                    width: double.infinity,
                    height: 60,
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.black,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(40),
                        ),
                      ),
                      onPressed: () {
                        if (role != null) {
                          _agoraNativePlugin.joinClassRoom(
                              roomId, userName, role!);
                        }
                      },
                      child: const Text(
                        "Quick Join Now",
                        style: TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                        ),
                      ),
                    ),
                  )
                ],
              ),
            ),
          ),
          // Right Image Section
          Expanded(
            flex: 2,
            child: Container(
              decoration: const BoxDecoration(
                image: DecorationImage(
                  image: AssetImage(
                      'assets/bg_image.png'), // <-- Replace with your image
                  fit: BoxFit.cover,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  InputDecoration _inputDecoration() {
    return InputDecoration(
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: Colors.grey),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(12),
        borderSide: const BorderSide(color: Colors.grey),
      ),
    );
  }
}
