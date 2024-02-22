package com.med.data.storage.mqtt

enum class MqttStatus {
    Connected,
    ConnectionLost,
    ConnectFailure,
    Disconnected,
    None
}