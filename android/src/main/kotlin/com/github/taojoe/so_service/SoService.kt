package com.github.taojoe.so_service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


class SoService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent?.action==Action.FOREGROUND_SERVICE_START.name){
            val bundle = intent.extras!!
            val activityIntent= bundle.get(Names.INTENT_NOTIFICATION_ACTIVITY_INTENT) as Intent
            val pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0)
            val notificationConfig=bundle.get(Names.INTENT_NOTIFICATION) as NotificationConfig
            val notificationChannelConfig=bundle.get(Names.INTENT_NOTIFICATION_CHANNEL) as NotificationChannelConfig
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(notificationChannelConfig.id, notificationChannelConfig.name, notificationChannelConfig.importance)
                channel.description="test"
                channel.enableLights(true)
                channel.lightColor= Color.BLUE
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .createNotificationChannel(channel)
            }
            println("!!!!!!")
            println(notificationConfig.id)
            println(notificationConfig.title)
            println(notificationConfig.content)
            println(notificationChannelConfig.id)
            val builder = NotificationCompat.Builder(this, notificationChannelConfig.id)
                    .setSmallIcon(getNotificationIcon(notificationConfig.icon))
                    .setContentTitle(notificationConfig.title)
                    .setContentText(notificationConfig.content)
                    .setContentInfo("a")
                    .setTicker("aaaa")
                    //.setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    //.setOngoing(true)
            if(notificationConfig.subtext?.isEmpty()!=true && false){
                builder.setSubText(notificationConfig.subtext)
            }
            val notification=builder.build()
            startForeground(notificationConfig.id, notification)
        }else if(intent?.action ==Action.FOREGROUND_SERVICE_STOP.name){
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private fun getNotificationIcon(iconName: String): Int {
        return applicationContext.resources.getIdentifier(iconName, "drawable", applicationContext.packageName)
    }
}