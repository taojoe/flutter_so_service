import 'dart:async';

import 'package:flutter/services.dart';

class NotificationChannelConfig{
  final String id;
  final String name;
  final int importance;

  NotificationChannelConfig(this.id, this.name, this.importance);

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id':this.id,
    'name':this.name,
    'importance':this.importance,
  };
}

class NotificationConfig{
  final int id;
  final String icon;
  final String title;
  final String content;
  final String subtext;
  final bool chronometer;
  final bool holdWakeLock;

  NotificationConfig(this.id, this.icon, this.title, this.content, this.subtext, this.chronometer, this.holdWakeLock);

  Map<String, dynamic> toJson() => <String, dynamic>{
    'id':this.id,
    'icon':this.icon,
    'title':this.title,
    'content':this.content,
    'subtext':this.subtext,
    'chronometer':this.chronometer,
    'holdWakeLock':this.holdWakeLock,
  };
}

class SoService {
  static const MethodChannel _methodChannel = const MethodChannel('com.github.taojoe.so_service/method');

  static Future<String> get platformVersion async {
    final String version = await _methodChannel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> startForegroundService(NotificationConfig notification, NotificationChannelConfig channel) async{
    final data=notification.toJson();
    if(channel!=null){
      data['channelConfig']=channel.toJson();
    }
    return await _methodChannel.invokeMethod<bool>('startForegroundService', data);
  }
}
