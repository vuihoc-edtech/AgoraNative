import 'dart:async';

import 'package:vh_agora_native/agora_native.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _agoraNativePlugin = AgoraNative();
  bool saved = false;
  String roomId = '2719 215 4979';
  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _agoraNativePlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Running on: $_platformVersion\n'),
              Text('Logged in: $saved'),
              TextField(
                onChanged: (value) {
                  roomId = value;
                },
              ),
              ElevatedButton(
                onPressed: () async {
                  // final res = await _agoraNativePlugin.login();
                  // setState(() {
                  //   saved = res;
                  // });
                },
                child: const Text("Login"),
              ),
              ElevatedButton(
                onPressed: () {
                  _agoraNativePlugin
                      .joinClassRoom(roomId.trim().replaceAll(' ', ''));
                },
                child: const Text("Join Room"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
