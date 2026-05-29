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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {
    private var tts: TextToSpeech? = null

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("TASK_ID", -1)
        val taskName = intent.getStringExtra("TASK_NAME") ?: "Reminder"

        if (taskId != -1L) {
            triggerVibration(context)
            showNotification(context, taskName)
            speakTaskName(context, taskName)

            // Schedule next occurrence
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val db = AppDatabase.getDatabase(context)
                val task = db.taskDao().getTaskById(taskId)
                task?.let {
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

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun speakTaskName(context: Context, taskName: String) {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let {
                    it.language = Locale.US
                    it.speak(taskName, TextToSpeech.QUEUE_FLUSH, null, "LiteLoopTTS")
                }
            }
        }
    }
}
