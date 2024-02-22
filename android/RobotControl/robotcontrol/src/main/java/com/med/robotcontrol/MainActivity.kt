package com.med.robotcontrol

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var vm: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm = ViewModelProvider(this, MainViewModelFactory(applicationContext,this))
            .get(MainViewModel::class.java)

        requestPermission("android.permission.POST_NOTIFICATIONS",1)


        supportFragmentManager.beginTransaction().replace(
            R.id.nav_host_fragment,
            LiftControlFragment()
        ).commit()

    }



    fun MqttMessageHandler(message: com.med.domain.models.Message) {
        if (message.isModule()) {
            findViewById<TextView>(R.id.tv_status).text  = message.data
        }
    }

    fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }
}