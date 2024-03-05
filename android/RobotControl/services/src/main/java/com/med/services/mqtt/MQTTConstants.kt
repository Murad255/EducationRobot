package com.med.services.mqtt

import com.med.domain.MQTT_QOS

val MQTT_SERVER_LIST = arrayOf(
    MqttBrokerAddress("tcp://192.168.8.151:1883", "admin", "semen"),
    MqttBrokerAddress("tcp://82.146.60.95:1883", "admin1", "@dm!N"),
    MqttBrokerAddress("tcp://192.168.31.198:1883", "robot", "control"),
    MqttBrokerAddress("tcp://mqtt.eclipseprojects.io:1883")
)

const val GROUP = "userM"
const val IN_DEVICES         = "userM/devices/in"
const val OUT_DEVICES        = "userM/devices/out"

const val IN_WATCHER = "userM/devices/in" //"userM/watcher/in"
const val OUT_WATCHER = "userM/devices/out" //"userM/watcher/out"

const val IN_DATA = "userM/data/in"
const val OUT_DATA = "userM/data/out"

const val QOS = MQTT_QOS
const val notificationId = 105
const val chanelId = "channelId_mqtt"

const val LOG_TAG  = "MqttClient"
const val LOG_TAG_SERVICE  = "MqttClient"

const val CONNECTION_SUCCESS = "CONNECTION_SUCCESS"
const val CONNECTION_FAILURE = "CONNECTION_FAILURE"
const val CONNECTION_LOST = "CONNECTION_LOST"
const val DISCONNECT_SUCCESS = "DISCONNECT_SUCCESS"