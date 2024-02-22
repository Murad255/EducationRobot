package com.med.services.mqtt

enum class MqttStatus {
    Connected,
    ConnectionLost,
    ConnectFailure,
    Disconnected,
    None
}