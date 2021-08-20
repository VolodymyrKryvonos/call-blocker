package com.call_blocke.app.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.call_blocke.app.MainActivity
import com.call_blocke.app.R
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.SimUtil
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TaskExecutorService : Service() {

    companion object {
        val isRunning = MutableLiveData(false)
        fun start(context: Context) {
            context.startService(Intent(context, TaskExecutorService::class.java))
        }
        fun stop(context: Context) {
            context.stopService(Intent(context, TaskExecutorService::class.java))
        }
    }

    private var lastSimSlot
        get() = SmsBlockerDatabase.lastSimSlotUsed
        set(value) {
            SmsBlockerDatabase.lastSimSlotUsed = value
        }

    private val taskRepository = RepositoryImp.taskRepository

    private val settingsRepository = RepositoryImp.settingsRepository

    private val userRepository = RepositoryImp.userRepository

    private val mHandler = Handler(Looper.myLooper()!!)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()

        isRunning.postValue(true)

        doWork()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(worker)
        isRunning.postValue(false)
    }

    @DelicateCoroutinesApi
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @DelicateCoroutinesApi
    private fun doWork() {
        GlobalScope.launch(Dispatchers.IO) {
            //taskRepository.deleteUnProcessed()

            taskRepository.reloadTasks()

            val tasks = taskRepository.toProcessList()

            Log.d("TaskExecutorService", "tasks is $tasks")

            tasks.forEach { task ->
                delay(2 * 1000L)

                updateSimSlot()

                val simInfo: SubscriptionInfo = SimUtil.getSIMInfo(applicationContext)[lastSimSlot]

                taskRepository.taskOnProcess(
                    taskEntity = task,
                    simSlot = lastSimSlot
                )

                var isOK = sendSms(applicationContext, simInfo, task)

                if (!isOK && SimUtil.getSIMInfo(applicationContext).size > 1) {
                    updateSimSlot()
                    taskRepository.taskOnProcess(
                        taskEntity = task,
                        simSlot = lastSimSlot
                    )
                    isOK = sendSms(applicationContext, SimUtil.getSIMInfo(applicationContext)[lastSimSlot], task)
                }

                if (isOK)
                    taskRepository.taskOnDelivered(task)
                else
                    taskRepository.taskOnError(task)

                taskRepository.confirmTasksStatus()
            }

            if (tasks.isNotEmpty()) {
                settingsRepository.reloadBlackList(applicationContext)

                val systemDetail = userRepository.systemDetail()

                if (systemDetail.deliveredCount >= (
                            settingsRepository.currentSmsContFirstSimSlot +
                                    settingsRepository.currentSmsContSecondSimSlot)) {
                    notifySmsLimitDone()
                }
            }

            if (isRunning.value == true)
                mHandler.postDelayed(worker, (if (tasks.isEmpty()) 10 else 1) * 1000L)
        }
    }

    @DelicateCoroutinesApi
    private val worker = Runnable {
        doWork()
    }

    private fun updateSimSlot() {
        val sims = SimUtil.getSIMInfo(applicationContext)

        if (sims.size > 1) {
            lastSimSlot = if (lastSimSlot == 0) 1 else 0
        }

        lastSimSlot = 0
    }

    private fun startForeground() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val channelId =
            createNotificationChannel("my_service", "My Background Service")

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Task executor")
            .setContentText("On run")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        this.startForeground(88, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun notifySmsLimitDone() {
        val channelId =
            createNotificationChannel("my_service", "My Background Service")

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle(applicationContext.getString(R.string.service_sms_limit_done_title))
            .setContentText(applicationContext.getString(R.string.service_sms_limit_done_desc))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(1, notification)
    }
}

suspend fun sendSms(context: Context, simInfo: SubscriptionInfo, task: TaskEntity): Boolean {
    return sendSms(context, simInfo, task.sendTo, task.message)
}

suspend fun sendSms(context: Context, simInfo: SubscriptionInfo, address: String, text: String): Boolean = suspendCoroutine { cont ->
    val sentRegisterName = "SMS_SENT"

    val smsManager: SmsManager = try {
        SmsManager.getSmsManagerForSubscriptionId(simInfo.subscriptionId)
    } catch (e: Exception) {
        e.printStackTrace()
        cont.resume(false)
        return@suspendCoroutine
    }

    val sentStatusIntent = Intent(sentRegisterName)

    object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, arg1: Intent) {
            cont.resume(resultCode == Activity.RESULT_OK)
            context.unregisterReceiver(this)
        }
    }.also {
        context.registerReceiver(it, IntentFilter(sentRegisterName))
    }

    val sentPI = PendingIntent.getBroadcast(context, address.hashCode(), sentStatusIntent, 0)

    smsManager.sendTextMessage(
        address,
        null,
        text,
        sentPI,
        null
    )
}