package com.med.data.storage

interface INotificationStorage {
    fun sentNotification(tag: String, message:String)
}