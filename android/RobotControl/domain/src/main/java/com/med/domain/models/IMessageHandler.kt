package com.med.domain.models

interface IMessageHandler {
    fun Check(message: Message?): Boolean
    fun Process(message: Message?)
}