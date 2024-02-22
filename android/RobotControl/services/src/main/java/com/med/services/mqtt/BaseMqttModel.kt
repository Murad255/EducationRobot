package com.med.services.mqtt

import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttMessageListener

interface BaseMqttModel {
    fun connectToServer()

    fun disconnectFromServer()
//
//    fun subscribeToTopic(topicName: String, qos: Int, subscriptionListener: IMqttActionListener?, messageListener: IMqttMessageListener?)
//    fun subscribeToTopic(topicName: String)
//
//    fun unsubscribeFromTopic(topicName: String)
}