package com.example.liteloop.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.example.liteloop.R
import com.example.liteloop.data.AppDatabase
import com.example.liteloop.scheduler.ReminderScheduler
import com.example.liteloop.util.LLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    private var tts: TextToSpeech? = null
    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("TASK_ID", -1)
        val taskName = intent.getStringExtra("TASK_NAME") ?: "Reminder"

        LLog.d(TAG, "onReceive: Task ID $taskId, Name: $taskName")

        if (taskId != -1L) {
            triggerVibration(context)
            showNotification(context, taskName)
            speakTaskName(context, taskName)

            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val db = AppDatabase.getDatabase(context)
                val task = db.taskDao().getTaskById(taskId)
                task?.let {
                    LLog.d(TAG, "Scheduling next alarm for ${it.name}")
                    ReminderScheduler(context).scheduleNextAlarm(it)
                }
            }
        }
    }

    private fun triggerVibration(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun showNotification(context: Context, taskName: String) {
        val channelId = "liteloop_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "LiteLoop Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("LiteLoop")
            .setContentText(taskName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskIdToNotificationId(taskName), notification)
    }

    private fun taskIdToNotificationId(name: String): Int {
        return name.hashCode()
    }

    private fun speakTaskName(context: Context, taskName: String) {
        LLog.d(TAG, "Attempting to speak: $taskName")
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let {
                    it.language = Locale.US
                    it.speak(taskName, TextToSpeech.QUEUE_FLUSH, null, "LiteLoopTTS")
                }
            } else {
                LLog.e(TAG, "TTS Initialization failed with status: $status")
            }
        }
    }
}
