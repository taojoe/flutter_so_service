package com.github.taojoe.so_service

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

enum class Action{
  FOREGROUND_SERVICE_START, FOREGROUND_SERVICE_STOP
}

object Names{
  val INTENT_NOTIFICATION="notification"
  val INTENT_NOTIFICATION_CHANNEL="notificationChannel"
  val INTENT_NOTIFICATION_ACTIVITY_INTENT="activity"
}

data class NotificationChannelConfig(val id:String, val name:String, val importance:Int) : Parcelable {
  constructor(parcel: Parcel) : this(
          parcel.readString(),
          parcel.readString(),
          parcel.readInt()) {
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(id)
    parcel.writeString(name)
    parcel.writeInt(importance)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<NotificationChannelConfig> {
    override fun createFromParcel(parcel: Parcel): NotificationChannelConfig {
      return NotificationChannelConfig(parcel)
    }

    override fun newArray(size: Int): Array<NotificationChannelConfig?> {
      return arrayOfNulls(size)
    }
  }
}

data class NotificationConfig(val id:Int, val icon:String, val title:String?, val content:String?, val subtext:String?, val chronometer:Boolean) : Parcelable {
  constructor(parcel: Parcel) : this(
          parcel.readInt(),
          parcel.readString(),
          parcel.readString(),
          parcel.readString(),
          parcel.readString(),
          parcel.readValue(Boolean::class.java.classLoader) as Boolean) {
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeInt(id)
    parcel.writeString(icon)
    parcel.writeString(title)
    parcel.writeString(content)
    parcel.writeString(subtext)
    parcel.writeValue(chronometer)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<NotificationConfig> {
    override fun createFromParcel(parcel: Parcel): NotificationConfig {
      return NotificationConfig(parcel)
    }

    override fun newArray(size: Int): Array<NotificationConfig?> {
      return arrayOfNulls(size)
    }
  }
}

fun Map<String, Any?>.asNotificationChannelConfig():NotificationChannelConfig{
  return NotificationChannelConfig(
          this["id"] as String,
          this["name"] as String,
          this["importance"] as Int
  )
}

fun Map<String, Any?>.asNotificationConfig():NotificationConfig{
  return NotificationConfig(
          this["id"] as Int? ?:1,
          this["icon"] as String,
          this["title"] as String?,
          this["content"] as String?,
          this["subtext"] as String?,
          this["chronometer"] as Boolean
  )
}

/** SoServicePlugin */
public class SoServicePlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    init(flutterPluginBinding.binaryMessenger, flutterPluginBinding.applicationContext)
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    val METHOD_CHANNEL_NAME="com.github.taojoe.so_service/method"
    lateinit var methodChannel: MethodChannel
    lateinit var context: Context
    private var activityBinding: ActivityPluginBinding? = null
    private var registrar: Registrar?=null
    private val currentActivity: Activity?
      get() = activityBinding?.activity ?: registrar?.activity()
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      this.registrar=registrar
      init(registrar.messenger(), registrar.context())
    }
    private fun init(messenger: BinaryMessenger, context: Context){
      methodChannel = MethodChannel(messenger, METHOD_CHANNEL_NAME)
      methodChannel.setMethodCallHandler(SoServicePlugin())
      this.context=context
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if(call.method=="startForegroundService"){
      val notificationMap =call.arguments<Map<String, Any?>>()
      val notification=notificationMap.asNotificationConfig()
      val channelConfigMap=call.argument<Map<String, Any?>?>("channelConfig")
      val channelConfig=channelConfigMap?.asNotificationChannelConfig()
      startForegroundService(notification, channelConfig)
      result.success(true)
    }else if(call.method=="stopForegroundService"){
      stopForegroundService()
      result.success(true)
    }else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
  }

  override fun onDetachedFromActivity() {
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activityBinding=binding
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    onReattachedToActivityForConfigChanges(binding)
  }

  override fun onDetachedFromActivityForConfigChanges() {
  }

  fun startForegroundService(notificationConfig:NotificationConfig, notificationChannelConfig: NotificationChannelConfig?){
    val intent = Intent(context, SoService::class.java)
    intent.action = Action.FOREGROUND_SERVICE_START.name
    intent.putExtra(Names.INTENT_NOTIFICATION, notificationConfig)
    intent.putExtra(Names.INTENT_NOTIFICATION_CHANNEL, notificationChannelConfig ?: NotificationChannelConfig("so_service_channel_id", "so_service_channel_name", NotificationManager.IMPORTANCE_HIGH))
    intent.putExtra(Names.INTENT_NOTIFICATION_ACTIVITY_INTENT, Intent(context, currentActivity!!.javaClass).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    })
    currentActivity!!.startService(intent)
  }
  fun stopForegroundService(){
    val intent = Intent(context, SoService::class.java)
    intent.action = Action.FOREGROUND_SERVICE_STOP.name
    currentActivity!!.startService(intent)
  }
}

