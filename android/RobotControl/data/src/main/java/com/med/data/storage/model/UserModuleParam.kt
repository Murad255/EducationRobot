package com.med.data.storage.model

import com.med.domain.models.Message
import kotlin.reflect.KFunction1

class UserModuleParam(val name:String, val type: String, val messageHandler: KFunction1<Message, Unit>)