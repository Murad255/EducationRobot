package com.med.data.repository

import com.med.data.storage.PushNotificationStorage
import com.med.domain.repository.INotificationRepo

class NotificationRepo (val notificationStorage: PushNotificationStorage): INotificationRepo {

    override fun sentNotification(tag: String, message: String) {
        notificationStorage.sentNotification(tag, message)
    }
}