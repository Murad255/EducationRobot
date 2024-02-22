package com.med.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import com.med.domain.IS_DEBUG_VERSION
import com.med.domain.models.Message
import com.med.domain.repository.INotificationRepo
import com.med.services.mqtt.*
import com.med.services.mqtt.MqttClient
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import java.util.ArrayList

class ModuleMqttService : Service() {

    private var mqttClientList: ArrayList<MqttClient> = ArrayList() // список Mqtt клиентов
    lateinit var mqttCliendId: String

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    private val context = this
    //private var mqttHadlerParam = MqttModuleParam("all","User",::mqttHandler)

    var notificationExternalRepo: INotificationRepo? = null

    companion object {
        val MQTT_CONNECT = "mqtt_connect"
        val MQTT_DISCONNECT = "mqtt_disconnect"

        val NOTIFICATION_CHANEL_ID = "HOSPITAL SERVICE CHANNEL"
        val NOTIFICATION_NAME = "Сервис приёма уведомлений"
        val NOTIFICATION_DESCRIPTION = "Сервис приёма уведомлений запущен"
    }

    override fun onBind(intent: Intent?) = mBinder

    override fun onCreate() {
        super.onCreate()
        mqttCliendId = chanelId
        val notification = createNotification()
        startForeground(1, notification)
        Log.d(LOG_TAG_SERVICE,"Created mqtt service...")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            Log.d(LOG_TAG_SERVICE,"using an intent with action $action")
            when (action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> Log.d(LOG_TAG_SERVICE,"This should never happen. No action in the received intent")
            }
        } else {
            Log.d(LOG_TAG_SERVICE,
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    fun getMqttServiceContext(): Context{
        return this
    }
    /**
     * Connects to the Mqtt Server.
     */
    fun connectToServer() {
        for ( client in MQTT_SERVER_LIST) {
           var tempClientL = mqttClientList.filter { clnt -> clnt.getUrl().equals(client.url) }
            if (tempClientL.isNotEmpty()) {
                var tempClient = tempClientL[0]
                if (tempClient.isConnected())
                    continue
                mqttClientList.remove(tempClient)
            }
            val mqttClient =  MqttClient(context, client, mqttCliendId,false)
            mqttClientList.add(mqttClient)
        }
        Log.d(LOG_TAG_SERVICE,"Mqtt Client connect")
    }

    /**
     * Disconnect from the Mqtt Server.
     */
    fun disconnectFromServer() {
        for ( client in mqttClientList) {
            client.disconnect()
        }
    }

    fun subscribe(topic: String){
        for ( client in mqttClientList) {
            client.subscribe(topic)
        }
    }

    private fun startService() {
        if (isServiceStarted) return
        Log.d(LOG_TAG_SERVICE,"Starting ModuleMqttService")
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ModuleMqttService::lock").apply {
                    acquire()
                }
            }
        connectToServer()
        var delayCounter = 0
        var delayCounter2 = 59*6
        val notificationRepo = getForegroundNotificationRepo()
        if (IS_DEBUG_VERSION)
           scheduleCheckConnectJob(this,::checkConnectJob)

        CoroutineScope(CoroutineName("MainLooper")).launch(Dispatchers.IO) {
            while (isServiceStarted) {
                delay(5000)
                try {
                    launch(Dispatchers.IO) {
                        checkConnect()
                    }
                    delayCounter++
                    delayCounter2++
                    // once in 5 min trying connect all brokers
                    if (delayCounter >= 60) {
                        connectToServer()
                        delayCounter = 0
                    }
                    // once in 30 min trying notify
                    if (delayCounter2 >= 60 * 6 && IS_DEBUG_VERSION) {
                        Log.d(
                            LOG_TAG_SERVICE,
                            "NOTIFY///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////"
                        )
                        notificationRepo.sentNotification("test", "проверка сервиса уведомлений")
                        if (notificationExternalRepo != null)
                            notificationExternalRepo!!.sentNotification(
                                "test notificationExternalRepo",
                                "проверка сервиса уведомлений"
                            )
                        delayCounter2 = 0

                    }
                }
                catch (e: Exception){
                    Log.e(LOG_TAG_SERVICE,"Error checkConnect")
                }
            }
        }
    }
    var nid = 300
    private fun checkConnectJob(count: Int){
        val notificationRepo = getForegroundNotificationRepo(nid)
        nid+=1
        notificationRepo.sentNotification("job", "проверка работы планировщика")
    }

    private fun checkConnect(){
        try {
            if (!isConnected()) {
                connectToServer()
                return
            }

            for (client in mqttClientList) {
                val connectStatus = client.getStatus() //isConnected()
                Log.d(LOG_TAG_SERVICE, "checkConnect to ${client.getUrl()} is $connectStatus")

                if (connectStatus == MqttConnectStatus.ConnectionLost ||
                    connectStatus == MqttConnectStatus.SubscribeFailure
                ) {
                    client.connect()
                }
            }
        }
        catch (e:Exception){
            Log.e(LOG_TAG_SERVICE, "checkConnect error. ${e.message}")
        }
    }

    private fun stopService() {
        log("Stopping the foreground service")
        Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
        disconnectFromServer()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.d(LOG_TAG_SERVICE,"Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }
    
    /**
     * Publish a [MqttMessage] into the specified [topicName].
     */
    fun publish(topicName: String, message: String){
        for ( client in mqttClientList) {
            client.publish(topicName, message)
        }
    }

    override fun onDestroy() {
        getForegroundNotificationRepo().sentNotification("onDestroy", "закрытие сервиса уведомлений")

        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
    }

    fun isConnected(): Boolean {
        var connectCount = 0
        for ( client in mqttClientList)
            if (client.isConnected())
                connectCount++
        Log.d(LOG_TAG, "connect count = $connectCount")
        return connectCount>0
    }

    fun setUserParam(param: MqttModuleParam){

        param
        for ( client in mqttClientList)
            client.setUserParam(param)
    }

    private fun createNotification(): Notification {
        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                NOTIFICATION_CHANEL_ID,
                NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = NOTIFICATION_DESCRIPTION
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

//        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
//            PendingIntent.getActivity(this, 0, notificationIntent, 0)
//        }
        val serviceIntent = Intent(applicationContext, ModuleMqttService::class.java).also {
            it.setPackage(packageName)
        }
        val pendingIntent: PendingIntent //= PendingIntent.getActivities(context,0, arrayOf(intent),0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // FLAG_MUTABLE
            pendingIntent = PendingIntent.getService(this, 1, serviceIntent, PendingIntent.FLAG_IMMUTABLE);

        }else {
            pendingIntent = PendingIntent.getService(this, 1, serviceIntent, PendingIntent.FLAG_MUTABLE);
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            NOTIFICATION_CHANEL_ID
        ) else Notification.Builder(this)

        return builder
            .setContentTitle(NOTIFICATION_NAME)
            .setContentText(NOTIFICATION_DESCRIPTION)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_notifications)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

//    fun mqttHandler(mes: Message){
//
//    }


    fun getForegroundNotificationRepo(notificationId: Int  = 123):INotificationRepo{
        val notificationIcon = BitmapFactory.decodeResource(context.resources, R.drawable.mind)
        val repo = ForegroundNotificationRepo(context,notificationIcon,notificationId,
            "ForegroundNotificationRepo_$notificationId"
        )
        return repo
    }

    public class ForegroundNotificationRepo (private val context: Context, private  val icon: Bitmap,
                                                val notificationId: Int, val chanelId: String ):
        INotificationRepo {

        private val notification = Notification(context,icon, notificationId, chanelId)

        init {
            createNotificationChannel()
        }
        fun createNotificationChannel(){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val name = "Not Title"
                val descriptionText = "Not description"
                val importance: Int = NotificationManager.IMPORTANCE_DEFAULT

                val channel: NotificationChannel =  NotificationChannel (chanelId, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel (channel)
            }
        }
        override fun sentNotification(tag: String, message: String) {
            notification.sentNotification(tag,message,context)
        }
    }

    private val mBinder = LocalBinder()
    inner class LocalBinder : Binder() {
        val service: ModuleMqttService
            get() = this@ModuleMqttService
    }

}