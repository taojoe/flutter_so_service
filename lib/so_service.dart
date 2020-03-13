import 'dart:async';

import 'package:flutter/services.dart';

class SoService {
  static const MethodChannel _channel =
      const MethodChannel('so_service');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
