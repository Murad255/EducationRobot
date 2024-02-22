package com.med.data.storage.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.med.data.R


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
        Log.d("Notification","sentNotification")

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
        val action = NotificationCompat.Action.Builder(
            R.drawable.open_in,
            "Открыть приложение",
            pendingIntent
        ).build()
        val builder = NotificationCompat.Builder(currentContext, chanelId)
            //.setSmallIcon(R.drawable.mind)
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .setContentTitle(tag)
            .setContentText(message)
                //.setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setLargeIcon(icon)
            .setContentIntent(pendingIntent)
            .addAction(action)


        with(NotificationManagerCompat.from(currentContext)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(notificationId, builder.build()) // посылаем уведомление
            Log.d("Notification","sent build Notification")

        }
    }

}