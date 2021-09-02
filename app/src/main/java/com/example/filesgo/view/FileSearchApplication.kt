package com.example.filesgo.view

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.filesgo.utils.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FileSearchApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        constructNotification()
    }

    private fun constructNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                Constants.CHANNEL_ID,
                Constants.SEARCH_RESULT,
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}