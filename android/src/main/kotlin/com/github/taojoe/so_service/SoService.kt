package com.github.taojoe.so_service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
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
            val pm = applicationContext.packageManager
            val notificationIntent = pm.getLaunchIntentForPackage(applicationContext.packageName)
            val pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0)
            val bundle = intent.extras
            val notificationConfig=bundle.get(Names.INTENT_NOTIFICATION) as NotificationConfig
            val notificationChannelConfig=bundle.get(Names.INTENT_NOTIFICATION_CHANNEL) as NotificationChannelConfig
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(notificationChannelConfig.id, notificationChannelConfig.name, notificationChannelConfig.importance)
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .createNotificationChannel(channel)
            }
            val builder = NotificationCompat.Builder(this, notificationChannelConfig.id)
                    .setSmallIcon(getNotificationIcon(notificationConfig.icon))
                    .setContentTitle(notificationConfig.title)
                    .setContentText(notificationConfig.content)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .setUsesChronometer(notificationConfig.chronometer)
                    .setOngoing(true)
            if(notificationConfig.subtext?.isEmpty()!=true){
                builder.setSubText(notificationConfig.subtext)
            }
            startForeground(notificationConfig.id, builder.build())
        }else if(intent?.action ==Action.FOREGROUND_SERVICE_STOP.name){
            stopForeground(Service.STOP_FOREGROUND_DETACH)
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private fun getNotificationIcon(iconName: String): Int {
        return applicationContext.resources.getIdentifier(iconName, "drawable", applicationContext.packageName)
    }
}