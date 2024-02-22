package com.med.services.mqtt

import android.util.Log
import info.mqtt.android.service.MqttTraceHandler

class CustomMqttLog : MqttTraceHandler {


    override fun traceDebug(message: String?) {
        if (message!=null)
            Log.i(LOG_TAG_SERVICE,message)
    }

    override fun traceError(message: String?) {
        if (message!=null)
            Log.d(LOG_TAG_SERVICE,message)    }

    override fun traceException(message: String?, e: Exception?) {
        if (message!=null)
            Log.e(LOG_TAG_SERVICE,message)
    }
}