package com.med.domain.repository

import com.med.domain.models.IMessageHandler

interface IMessageHandlersRepository {
    fun AddHandler(handler: IMessageHandler?)
}