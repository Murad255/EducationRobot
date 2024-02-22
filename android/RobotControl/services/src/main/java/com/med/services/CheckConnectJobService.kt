package com.med.services

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import android.util.Log
import kotlin.reflect.KFunction1

class CheckConnectJobService () : JobService() {

    override fun onStartJob(@SuppressLint("SpecifyJobSchedulerIdRange") params: JobParameters?): Boolean {
        // Задача, выполняемая в фоновом режиме
        Log.d(TAG, "Job started")
        // Завершение задачи
        //CoroutineScope(Dispatchers.IO).launch {
            checkFunc?.let { it(1) }
            // Ваш код для выполнения фоновой работы
            Log.d(TAG, "Job running")

            // Пометить задачу как завершенную
            jobFinished(params, false)
       // }

        return true // Возвращает true, чтобы показать, что задача выполняется асинхронно
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        // Вызывается, если выполнение задачи было прервано
        Log.d(TAG, "Job stopped")
        return true // Возвращает true, чтобы перепланировать задачу
    }

    companion object {
        val timeoutSec = 5L
         val TAG = "CheckConnectJobService"
        var checkFunc: KFunction1<Int, Unit>? = null
    }
}

fun scheduleCheckConnectJob(context: Context, checkFunc: KFunction1<Int, Unit>) {
    CheckConnectJobService.checkFunc = checkFunc
    val jobServiceComponent = ComponentName(context, CheckConnectJobService::class.java)
    val builder = JobInfo.Builder(12345, jobServiceComponent)

    // Настройка параметров задачи
    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
    builder.setRequiresCharging(false)

    // Передача данных в задачу (необязательно)
    val extras = PersistableBundle()
   // extras.putString("checkFunc", "value")
    builder.setExtras(extras)
    builder.setPeriodic(CheckConnectJobService.timeoutSec*1000 )
    builder.setBackoffCriteria(6000, JobInfo.BACKOFF_POLICY_LINEAR)
    builder.setPersisted(true) // вкл после перезагрузки смартфона

    val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    // Планирование задачи
    jobScheduler.schedule(builder.build())
}