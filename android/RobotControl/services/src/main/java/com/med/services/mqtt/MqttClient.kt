package com.med.services.mqtt

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.med.services.R

import com.med.domain.models.Message
import info.mqtt.android.service.Ack
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.*
import org.eclipse.paho.client.mqttv3.*

class MqttClient(val context: Context, val address: MqttBrokerAddress, val chanelId: String, val isForeground: Boolean = true) {

    private val rnds = (1000..1000000).random()

    private var client: MqttAndroidClient? = null
    private var userParam: MqttModuleParam?= null
    private var connectStatus = MqttConnectStatus.None

    private var clientId = "user_$rnds"
    var pastMessage: Message? = null
    var isSubscribe: Boolean = false

    @Volatile var msgNotPublish: Int = 0
    var rejectionMessage =  RejectionMessage()


    val defaultCbConnect = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            val client = asyncActionToken?.client as MqttAndroidClient? ?: return
            Log.i(LOG_TAG, "Connection success to " + client.serverURI)

            CoroutineScope(Dispatchers.IO).launch {
                val subscribeAction = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.i(LOG_TAG, "Subscribe to " + client.serverURI)
                        isSubscribe = true
                        connectStatus = MqttConnectStatus.Connected
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        Log.e(LOG_TAG, "Failed to subscribe: "+ client.serverURI)
                        connectStatus = MqttConnectStatus.SubscribeFailure
                    }
                }

                try {
                    isSubscribe = false
                    client.subscribe(IN_WATCHER, QOS, context, subscribeAction)
                } catch (e: MqttException) {
                    e.printStackTrace()
                    Log.d(LOG_TAG, "subscribe to MqttException")
                    connectStatus = MqttConnectStatus.SubscribeFailure
                }

            }

        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.w(LOG_TAG, "Connection failure: ${exception.toString()}")
            connectStatus = MqttConnectStatus.ConnectFailure
        }
    }

    private val defaultCbClient = object : MqttCallback {
        override fun messageArrived(topic: String?, message: MqttMessage?) {
            val msg = Message(message.toString(), topic)
            if((msg.UID != 0L || msg.UID != -1L) && !rejectionMessage.Check(msg.UID)) return

            if (msg.isModule()) {
                if(userParam!=null)
                    userParam!!.messageHandler(msg)
                else
                    Log.w(LOG_TAG, "MQTT client $address not have user param")
                pastMessage = msg
            }
            Log.d(LOG_TAG, "Receive from topic: $topic message ${message.toString()} ")
        }

        override fun connectionLost(cause: Throwable?) {
            Log.e(LOG_TAG, "Connection lost ${cause.toString()}")
            connectStatus = MqttConnectStatus.ConnectionLost
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            Log.i(LOG_TAG, "Delivery completed")
        }
    }

    private val defaultCbSubscribe = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(LOG_TAG, "Subscribed to topic")
        }
        override fun onFailure( asyncActionToken: IMqttToken?, exception: Throwable? ) {
            Log.e(LOG_TAG, "Failed to subscribe")
            connectStatus = MqttConnectStatus.SubscribeFailure
        }
    }
    private val defaultCbUnsubscribe = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(LOG_TAG, "Unsubscribed to topic")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.w(LOG_TAG, "Failed to unsubscribe topic")
        }
    }
    private val defaultCbPublish = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.d(LOG_TAG, "Message published to topic")
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.e(LOG_TAG, "Failed to publish message to topic")
            msgNotPublish += 1
        }
    }
    private val defaultCbDisconnect = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            Log.w(LOG_TAG, "Disconnected")
            connectStatus = MqttConnectStatus.Disconnected
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            Log.w(LOG_TAG, "Failed to disconnect")
        }
    }

    init {
        client = MqttAndroidClient(context, address.url, clientId, Ack.AUTO_ACK)
        connect()
        Log.i(LOG_TAG,"MqttClient  created")
    }

    fun setUserParam(param: MqttModuleParam){
        userParam = param
    }

    fun connect(cbConnect:  IMqttActionListener  = defaultCbConnect,
                cbClient:   MqttCallback         = defaultCbClient) {
        if (connectStatus == MqttConnectStatus.Connecting ||
            connectStatus == MqttConnectStatus.Connected)
            return
        var notificationBuilder: NotificationCompat.Builder? = null
        connectStatus = MqttConnectStatus.Connecting
        if (isForeground) {
            val intent: Intent = Intent(context, context.javaClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            var pendingIntent: PendingIntent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // FLAG_MUTABLE
                pendingIntent =
                    PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);

            } else {
                pendingIntent =
                    PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_MUTABLE );
            }
            notificationBuilder = NotificationCompat.Builder(
                context,
                chanelId
            )
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Сервис MQTT")
                .setContentText("Сервис приёма уведомлений работает ("+chanelId+")")
                //.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_LOW)//PRIORITY_DEFAULT)
               // .setCategory(NotificationCompat.CATEGORY_PROMO)
                .setSound(null)
                .setContentIntent(pendingIntent)

        }

        if (isForeground && notificationBuilder != null)
            client!!.setForegroundService(notificationBuilder.build())
        client!!.setCallback(cbClient)
        val options = MqttConnectOptions()
        options.apply {
            userName = address.login
            password = address.password.toCharArray()
            connectionTimeout = 30
            isAutomaticReconnect = true
            isCleanSession = true
            keepAliveInterval = 120
        }
        try {
            client!!.connect(options, null, cbConnect)
        } catch (e: MqttException) {
            Log.e(LOG_TAG, "can`t connect to " + address.url)
        }

    }

    fun isConnected(): Boolean {
        return  (client?.isConnected ?: false)
                && isSubscribe
                && connectStatus != MqttConnectStatus.ConnectFailure
    }

    fun getStatus(): MqttConnectStatus{
        return connectStatus
    }

    fun getUrl(): String{
        return address.url
    }

    fun subscribe(topic: String, cbSubscribe:  IMqttActionListener = defaultCbSubscribe) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client?.subscribe(topic, QOS, context, cbSubscribe)
            } catch (e: MqttException) {
                e.printStackTrace()
                Log.d(LOG_TAG, "subscribe to MqttException")
            }

        }
    }
    fun unsubscribe(topic: String, cbUnsubscribe:  IMqttActionListener = defaultCbUnsubscribe) {
        try {
            client?.unsubscribe(topic, context, cbUnsubscribe)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic:      String,
                msg:        String,
                retained:   Boolean = false):Boolean {
        val message = MqttMessage()
        message.payload = msg.toByteArray()
        message.qos = QOS
        message.isRetained = retained
        try {
            if (client != null && client!!.isConnected)
                client!!.publish(topic, message, null, defaultCbPublish)
            else
                return false
        } catch (e: MqttException) {
            e.printStackTrace()
            Log.w(LOG_TAG, "publish to global MqttException")
            return false
        }
        return true
    }

    fun publish(message: Message):Boolean {
       return publish(OUT_WATCHER, message.toString())
    }

    fun publishData(msg: String):Boolean {
        if(userParam!= null){
            var sentMes = "<Module><Name>"+ userParam!!.name+"</Name><ModuleType>"+userParam!!.type+
                    "</ModuleType><UID>" +Message.getUid().toString() +"</UID>"+
                    "<Data>"+msg+"</Data></Module>"
           return publish(OUT_WATCHER,sentMes)
        }
        return false
    }

    fun publishData(msg: String,moduleType: String):Boolean {
        if(userParam!= null){
            var sentMes = "<Module><Name>"+ userParam!!.name+"</Name><ModuleType>"+moduleType+
                    "</ModuleType><UID>" +Message.getUid().toString() +"</UID>"+
                    "<Data>"+msg+"</Data></Module>"
            return publish(OUT_WATCHER,sentMes)
        }
        return false
    }

    fun disconnect(cbDisconnect: IMqttActionListener = defaultCbDisconnect ) {
        try {
            client?.disconnect(context, cbDisconnect)
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

}