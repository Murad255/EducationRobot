package com.med.data.repository

import com.med.data.storage.IUserStorage
import com.med.domain.models.IMessageHandler
import com.med.domain.repository.IMessageHandlersRepository

class MessageHandlersRepositoryImpl ( val userStorage: IUserStorage): IMessageHandlersRepository {
    override fun AddHandler(handler: IMessageHandler?) {
        userStorage.AddHandler(handler)
    }
}