package com.med.services.mqtt

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage

class CustomMqttCallback(val context: Context) : MqttCallbackExtended {

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        log("Arrived message on topic $topic")
    }

    override fun connectionLost(cause: Throwable?) {
        log("ConnectionLost ${cause.toString()}")
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(CONNECTION_LOST))
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
    }

    private fun log(text: String) {
        Log.d("MQTT", text)
    }
}