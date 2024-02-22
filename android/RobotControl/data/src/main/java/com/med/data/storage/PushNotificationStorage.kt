package com.med.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.med.data.storage.notification.Notification

class PushNotificationStorage (private val context: Context, private  val icon: Bitmap, val notificationId: Int,val chanelId: String):
    INotificationStorage {

    private val notification = Notification(context,icon, notificationId, chanelId)

    init {
        notification.createNotificationChannel()
    }
    override fun sentNotification(tag: String, message: String) {
        Log.d("Notification","PushNotificationStorage")
        notification.sentNotification(tag,message,context)
    }
}