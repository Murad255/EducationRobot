package com.med.robotcontrol

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


import com.google.android.material.appbar.AppBarLayout
import com.jakchang.vertical_seekbar.VerticalSeekBar
import com.med.data.storage.PushNotificationStorage
import com.med.domain.models.*
import com.med.domain.usecase.*
import com.med.services.mqtt.ModuleMqttPresenter
import com.med.services.mqtt.MqttModuleParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel(
   // val notificationStorage: PushNotificationStorage,
    val showToastMessageUseCase: ShowToastMessageUseCase,
    val mqttService: ModuleMqttPresenter
): ViewModel() {

    val TAG = "MainViewModel"
    var appBarLayout: AppBarLayout? =null
    var group = ""
    private var _loginStatusLive = MutableLiveData<LoginStatus>(LoginStatus.Unknow)
    var loginStatusLive: LiveData<LoginStatus> = _loginStatusLive

    var user: User?= null


    init {
        Log.i(TAG,"VM  init")
        val userModuleParam = MqttModuleParam(
        "User",
        "User",
        ::MqttMessageHandler)
        mqttService.SetUserParam(userModuleParam)
    }

    fun MqttMessageHandler(message: com.med.domain.models.Message) {
        if (message.isModule()) {
            showToastMessageUseCase.execute( message.data)
        }
    }

    // обработчики кнопок
    fun stop(view: View){
        if (!CheckGroup()) return
        sentMqtt("stop",group+"/mode")
    }
    fun start(view: View){
        if (!CheckGroup()) return
        sentMqtt("start",group+"/mode")
    }
    fun set(view: View){
        if (!CheckGroup()) return
        sentMqtt("set",group+"/mode")
    }
    fun reset(view: View){
        if (!CheckGroup()) return
        sentMqtt("reset",group+"/mode")
    }
    fun save(view: View){
        if (!CheckGroup()) return
        sentMqtt("save",group+"/mode")
    }
    fun clear(view: View){
        if (!CheckGroup()) return
        sentMqtt("clear",group+"/mode")
    }
    fun back(view: View){
        if (!CheckGroup()) return
        sentMqtt("back",group+"/mode")
    }

    // обработчик слайдеров
    val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            // Обработайте изменения значения
            var join = -1
            when(seekBar.id){
                R.id.sb_join1 -> join = 1
                R.id.sb_join2 -> join = 2
                R.id.sb_join3 -> join = 3
                R.id.sb_join4 -> join = 4
               // R.id.sb_time ->  join = 5
            }
            if (join != -1)
                sentJoinPos(progress, join)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // Вызывается при начале перемещения ползунка
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            // Вызывается в конце перемещения ползунка
            var join = -1
            when(seekBar.id){
                R.id.sb_join1 -> join = 1
                R.id.sb_join2 -> join = 2
                R.id.sb_join3 -> join = 3
                R.id.sb_join4 -> join = 4
                R.id.sb_time ->  join = 0
            }
            if (join != -1)
                sentJoinPos(seekBar.progress, join)
        }
    }

    // обработчик выбора группы
    val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parentView: AdapterView<*>,
            selectedItemView: View,
            position: Int,
            id: Long
        ) {
            val selectedItem = parentView.getItemAtPosition(position).toString()
            group = selectedItem.toString()
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {
            // Выполняется, если ничего не выбрано
        }
    }

    fun sentMqtt(msg:String, topic:String){
        try {
            if(mqttService.isConnected())
                mqttService.publish(topic,msg)
            else
                showToastMessageUseCase.execute("Нет подключения к брокеру")
        }
        catch (e: Exception){
            showToastMessageUseCase.execute("ошибка")
        }
    }

    fun sentJoinPos(pos: Int, joinNum: Int){
        if (!CheckGroup()) return
        if (joinNum == 0)
            sentMqtt(pos.toString(),group+"/time")
        else
            sentMqtt(pos.toString(),group+"/join/"+joinNum)

    }

    fun CheckGroup(): Boolean{
        if (group.length <2){
            showToastMessageUseCase.execute("Не выбрана группа!")
            return false
        }
        return true
    }
}