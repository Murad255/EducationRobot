package com.med.services.mqtt;

public enum MqttConnectStatus {
    Connected,
    Connecting,
    ConnectionLost,
    ConnectFailure,
    SubscribeFailure,

    Disconnected,
    None
}
