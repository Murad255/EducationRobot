package com.med.services.mqtt

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.med.domain.models.Message
import com.med.domain.repository.INotificationRepo
import com.med.services.Actions
import com.med.services.ModuleMqttService
import com.med.services.ServiceState
import com.med.services.getServiceState

class ModuleMqttPresenter (val context: Context) {
    
    private var mqttService: ModuleMqttService? = null
    private lateinit var mqttBroadcast: MqttBroadcast
    private var userParam: MqttModuleParam?= null

    val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mqttService = (service as ModuleMqttService.LocalBinder).service
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            Toast.makeText(context, "Ошибка сопряжкня сервиса приёма сообщений", Toast.LENGTH_SHORT).show()
            mqttService = null
        }
    }

    init {
        connect()
        Log.i(LOG_TAG,"ModuleMqttPresenter  created")
    }

    fun SetUserParam(param: MqttModuleParam){
        userParam = param
        mqttService?.setUserParam(param)
    }

    fun SetNotificationRepo(repo: INotificationRepo){
        mqttService?.notificationExternalRepo = repo
    }

    fun  connect(){
        val startServiceIntent = actionOnService(Actions.START)
        if ( startServiceIntent != null)
            context.bindService(startServiceIntent, serviceConnection, 0)
    }

    private fun actionOnService(action: Actions) : Intent? {
        if (getServiceState(context) == ServiceState.STOPPED && action == Actions.STOP) return null
        return Intent(context, ModuleMqttService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.i(LOG_TAG,"Starting the service in >=26 Mode")
                context.startForegroundService(it)
            }
            else {
                Log.i(LOG_TAG, "Starting the service in < 26 Mode")
                context.startService(it)
            }
        }
    }

    fun stopMqttService() {
        context.unbindService(serviceConnection)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mqttBroadcast)
        val startServiceIntent = Intent(context, ModuleMqttService::class.java)
        startServiceIntent.action = ModuleMqttService.MQTT_DISCONNECT
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startServiceIntent)
        } else {
            context.startService(startServiceIntent)
        }
    }

    fun getMqttServiceContext(): Context {
        var cntxt: Context?  = null
        while (cntxt == null){
            cntxt = mqttService?.getMqttServiceContext()
        }
        return cntxt!!
    }
    
    fun isConnected(): Boolean {
        return mqttService?.isConnected() ?: false
    }

    fun publish(topic: String, msg: String, retained:Boolean = false):Boolean {
        if (mqttService == null)
            return false
        try {
            mqttService!!.publish(topic,msg)
        }
        catch (ex:Exception){
            Log.e(LOG_TAG,ex.message.toString())
            return false
        }
        return true
    }

    fun publish(message: Message):Boolean {
        return publish(OUT_WATCHER, message.toString())
    }

    fun publishData(msg: String):Boolean {
        if(userParam!= null){
            var sentMes = "<Module><Name>"+ userParam!!.name+"</Name><ModuleType>"+userParam!!.type+
                    "</ModuleType><UID>" + Message.getUid().toString() +"</UID>"+
                    "<Data>"+msg+"</Data></Module>"
            return publish(OUT_WATCHER,sentMes)
        }
        return false
    }

    fun publishData(msg: String,moduleType: String):Boolean {
        if(userParam!= null){
            var sentMes = "<Module><Name>"+ userParam!!.name+"</Name><ModuleType>"+moduleType+
                    "</ModuleType><UID>" + Message.getUid().toString() +"</UID>"+
                    "<Data>"+msg+"</Data></Module>"
            return publish(OUT_WATCHER,sentMes)
        }
        return false
    }

    fun subscribe(topic: String){
        mqttService?.subscribe(topic)
    }
    fun disconnect() {
        stopMqttService()
    }

    fun getForegroundNotificationRepo(): INotificationRepo?{
        return mqttService?.getForegroundNotificationRepo()
    }

    inner class MqttBroadcast: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            if(CONNECTION_SUCCESS == intent!!.action){
                Log.i(LOG_TAG, "Connect to service")
            }

            if(CONNECTION_FAILURE == intent.action){
                Log.i(LOG_TAG, "error on connecting service")
            }

            if(CONNECTION_LOST == intent.action){
                Log.i(LOG_TAG, "connection lost")
            }

            if(DISCONNECT_SUCCESS == intent.action){
                Log.i(LOG_TAG, "Disconnect on service")
            }
        }

    }

}