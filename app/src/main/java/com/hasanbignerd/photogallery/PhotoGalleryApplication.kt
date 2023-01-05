package com.hasanbignerd.photogallery

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

const val NOTIFICATION_CHANNEL_ID = "flick_poll"

class PhotoGalleryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

}