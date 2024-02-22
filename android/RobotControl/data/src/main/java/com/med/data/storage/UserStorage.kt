package com.med.data.storage

import android.util.Log
import com.med.data.storage.mqtt.IN_DEVICES
import com.med.data.storage.mqtt.OUT_WATCHER
import com.med.domain.models.IMessageHandler
import com.med.domain.models.LoginStatus
import com.med.domain.models.Message
import com.med.domain.models.UserLoginAnswer
import com.med.domain.models.UserLoginParam
import com.med.services.mqtt.IN_DATA
import com.med.services.mqtt.ModuleMqttPresenter
import com.med.services.mqtt.MqttModuleParam


/**
 * [UserStorage] обрабатывает принимаемые по Mqtt данные
 */
class UserStorage(val mqttClient: ModuleMqttPresenter) : IUserStorage {

    var userModuleParam: MqttModuleParam? = null
    var mqttHandlerList: ArrayList<IMessageHandler> =
        ArrayList() // список MqttAsyncClient, к которым удалось подключиться

    val TAG = "UserStorage"

    override fun AddHandler(handler: IMessageHandler?) {
        if (handler != null &&
            mqttHandlerList.filter { ihandler -> ihandler.javaClass == handler.javaClass }.isEmpty()
        ) {
            mqttHandlerList.add(handler)
        }
    }

    override suspend fun find(user: UserLoginParam): UserLoginAnswer {

        if (!mqttClient.isConnected()) {
            mqttClient.connect()
            return UserLoginAnswer(LoginStatus.Disconnected, null)
        }
        userModuleParam = MqttModuleParam(
            user.login,
            "User",
            ::MqttMessageHandler
        )
        mqttClient.SetUserParam(userModuleParam!!)
        var loginStr =
            "<Module><UserLogin>" + user.login + "</UserLogin><UserPwd>" + user.password + "</UserPwd><department>" + user.departament + "</department></Module>"
        if (user.departament != null)
            loginStr += "<department>" + user.departament + "</department>"
        loginStr += "</Module>"

        mqttClient.publish(OUT_WATCHER, loginStr)
        return UserLoginAnswer(LoginStatus.Unknow, null)
    }

    override fun sentLoginRequest(user: UserLoginParam): Boolean {
        if (!mqttClient.isConnected()) {
            mqttClient.connect()
            return false
        }
        userModuleParam = MqttModuleParam(user.login, "User", ::MqttMessageHandler)
        mqttClient.SetUserParam(userModuleParam!!)
        var loginStr =
            "<Module><UserLogin>" + user.login + "</UserLogin><UserPwd>" + user.password + "</UserPwd><ModuleType>User</ModuleType></Module>"
        return mqttClient.publish(OUT_WATCHER, loginStr)
    }

    override fun writeParams(user: UserLoginParam): Boolean {
        TODO("Not yet implemented")
    }


    override fun isConnectToStorage(): Boolean {
        return mqttClient.isConnected()
    }

    override fun subscribeTodataUpdate(dataType: String,dataId: Long){
        val topic = IN_DATA+"/"+dataType+"/"+dataId.toString()
        mqttClient.subscribe(topic)
    }
    override fun subscribeTodataUpdate(dataType: String){
        val topic = IN_DATA+"/"+dataType+"/#"
        mqttClient.subscribe(topic)
    }


    fun MqttMessageHandler(message: Message) {
        if (message.isModule) {
            if (message.topik.indexOf(IN_DEVICES)>=0)
                if( !message.name.equals(userModuleParam!!.name))
                return

            if (message.Request != null) {
                if (message.Request.equals("status")) {
                    var sentStr =
                        "<Module><Name>" + userModuleParam!!.name + "</Name><Status>0</Status></Module>"
                    mqttClient.publish(OUT_WATCHER, sentStr)
                }
            }
            for (handler in mqttHandlerList) {
                try {
                    if (handler.Check(message))
                        handler.Process(message)
                } catch (ex: java.lang.Exception) {
                    Log.e(TAG, "M-> messageArrived:" + ex.message)
                }
            }

        }
    }

}