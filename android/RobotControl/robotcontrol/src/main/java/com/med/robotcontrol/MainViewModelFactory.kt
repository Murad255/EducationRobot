package com.med.robotcontrol

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.med.data.repository.ToastMessageRepoImpl
import com.med.data.storage.UserStorage
import com.med.domain.usecase.*
import com.med.services.mqtt.ModuleMqttPresenter

class MainViewModelFactory(context: Context, activity: MainActivity): ViewModelProvider.Factory {

    private val notificationIcon  by lazy(LazyThreadSafetyMode.NONE) { BitmapFactory.decodeResource(context.resources, R.drawable.baseline_rocket) }
    val notificationId = 104
    val chanelId = "channelId_user"
    val sharedPreferences = context.getSharedPreferences(chanelId, Context.MODE_PRIVATE)

     private val mqttService = ModuleMqttPresenter(context)
    //private val mqttContext = mqttService.getMqttServiceContext()

    //STORAGES
    private val mqttStorage by lazy(LazyThreadSafetyMode.NONE){ UserStorage(mqttService )}
   // private val notificationStorage by lazy(LazyThreadSafetyMode.NONE) { PushNotificationStorage(context,notificationIcon, notificationId, chanelId) }

    //REPOSITORIES
    private val toastMessageRepo by lazy(LazyThreadSafetyMode.NONE) { ToastMessageRepoImpl (context) }

    //USECASES
    private val showToastMessageUseCase by lazy(LazyThreadSafetyMode.NONE) { ShowToastMessageUseCase(toastMessageRepo) }


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
            showToastMessageUseCase,
            mqttService
        ) as T
    }
}