package com.example.liteloop.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.liteloop.data.Task
import com.example.liteloop.receiver.AlarmReceiver
import com.example.liteloop.util.LLog
import java.util.*

class ReminderScheduler(private val context: Context?) {

    private val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val TAG = "Scheduler"

    fun scheduleNextAlarm(task: Task) {
        LLog.d(TAG, "scheduleNextAlarm for task: ${task.name} (ID: ${task.id})")
        
        if (context == null || alarmManager == null) {
            LLog.e(TAG, "Context or AlarmManager is null, cannot schedule")
            return
        }
        
        if (!task.isActive) {
            LLog.d(TAG, "Task is inactive, canceling any existing alarms")
            cancelAlarm(task)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                LLog.e(TAG, "Missing SCHEDULE_EXACT_ALARM permission")
                return
            }
        }

        val nextTime = calculateNextOccurrence(task)
        if (nextTime == null) {
            LLog.w(TAG, "Could not calculate next occurrence for task: ${task.name}")
            return
        }

        LLog.d(TAG, "Next alarm for '${task.name}' scheduled at: ${formatTime(nextTime)}")

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_NAME", task.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTime,
            pendingIntent
        )
    }

    fun cancelAlarm(task: Task) {
        if (context == null || alarmManager == null) return
        
        LLog.d(TAG, "Canceling alarm for task: ${task.name}")
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun calculateNextOccurrence(task: Task): Long? {
        return calculateNextOccurrenceAtTime(task, System.currentTimeMillis())
    }

    private fun formatTime(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return "%02d:%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))
    }

    fun calculateNextOccurrenceAtTime(task: Task, now: Long): Long? {
        val daysList = task.daysOfWeek.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
        if (daysList.isEmpty()) return null

        val today = Calendar.getInstance().apply { timeInMillis = now }
        
        for (i in 0..7) {
            val checkDate = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
            val dayOfWeek = checkDate.get(Calendar.DAY_OF_WEEK)
            val mappedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
            
            if (daysList.contains(mappedDay)) {
                val startCal = (checkDate.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, (task.startTime / 3600000).toInt())
                    set(Calendar.MINUTE, ((task.startTime % 3600000) / 60000).toInt())
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                val endCal = (checkDate.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, (task.endTime / 3600000).toInt())
                    set(Calendar.MINUTE, ((task.endTime % 3600000) / 60000).toInt())
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (task.endTime <= task.startTime) {
                    endCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                if (i == 0) {
                    if (now < startCal.timeInMillis) {
                        return startCal.timeInMillis
                    } else if (now >= startCal.timeInMillis && now < endCal.timeInMillis - 1000) {
                        val nextFreq = now + (task.frequencyMinutes * 60000)
                        return if (nextFreq < endCal.timeInMillis) nextFreq else null
                    }
                    
                    if (task.endTime <= task.startTime) {
                        val yesterdayStart = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                        val yesterdayEnd = (endCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                        
                        if (now >= yesterdayStart.timeInMillis && now < yesterdayEnd.timeInMillis - 1000) {
                            val nextFreq = now + (task.frequencyMinutes * 60000)
                            return if (nextFreq < yesterdayEnd.timeInMillis) nextFreq else null
                        }
                    }
                } else {
                    return startCal.timeInMillis
                }
            }
        }
        return null
    }
}
