package com.med.domain.repository

interface INotificationRepo {
    fun sentNotification(tag: String, message: String)
}