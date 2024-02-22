package com.med.services.mqtt

import com.med.domain.models.Message
import kotlin.reflect.KFunction1

class MqttModuleParam(val name:String, val type: String, val messageHandler: KFunction1<Message, Unit>)