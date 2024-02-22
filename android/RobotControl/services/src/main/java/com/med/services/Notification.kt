package com.med.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class Notification(private  val context: Context, private  val icon: Bitmap,val notificationId: Int,val chanelId: String) {//(private  val context: Context,activity : MainActivity?, private  val icon: Bitmap) {

//    companion object {
//        const val NOTIFICATION_ID = 102
//        const val CHANNEL_ID = "channelID2"
//    }

    //var main: MainActivity? = null


    fun createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Not Title"
            val descriptionText = "Not description"
            val importance: Int = NotificationManager.IMPORTANCE_HIGH

            val channel: NotificationChannel =  NotificationChannel (chanelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel (channel)
        }
    }

    fun sentNotification(tag: String,  message:String, currentContext: Context = context){
        val intent: Intent = Intent(currentContext, context.applicationContext.javaClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent //= PendingIntent.getActivities(context,0, arrayOf(intent),0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // FLAG_MUTABLE
            pendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        }else {
            pendingIntent = PendingIntent.getService(
                context,
                1,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            );
        }
        val builder = NotificationCompat.Builder(currentContext, chanelId)
            //.setSmallIcon(R.drawable.mind)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(tag)
            .setContentText(message)
                .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setLargeIcon(icon)
            .setContentIntent(pendingIntent)


        with(NotificationManagerCompat.from(currentContext)) {
            notify(notificationId, builder.build()) // посылаем уведомление
        }
    }

}