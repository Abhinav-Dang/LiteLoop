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
            action = "com.example.liteloop.ALARM_${task.id}"
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
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.liteloop.ALARM_${task.id}"
        }
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

                // Logic: Find the first multiple of frequency from startCal that is > now
                // within the window [startCal, endCal]
                
                val startTimeMillis = startCal.timeInMillis
                val endTimeMillis = endCal.timeInMillis
                val freqMillis = task.frequencyMinutes * 60000L

                if (now < startTimeMillis) {
                    // Before window starts today
                    return startTimeMillis
                } else if (now >= startTimeMillis && now < endTimeMillis - 1000) {
                    // Inside window
                    val elapsedSinceStart = now - startTimeMillis
                    val intervalsPassed = elapsedSinceStart / freqMillis
                    val nextRun = startTimeMillis + (intervalsPassed + 1) * freqMillis
                    
                    if (nextRun < endTimeMillis) {
                        return nextRun
                    }
                    // Else: past the last possible interval for this window, fall through to next day
                }
                
                // If it's overnight, check if we are in the tail end of yesterday's window
                if (task.endTime <= task.startTime && i == 0) {
                    val yesterdayStart = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                    val yesterdayEnd = (endCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                    
                    val yStartMillis = yesterdayStart.timeInMillis
                    val yEndMillis = yesterdayEnd.timeInMillis
                    
                    if (now >= yStartMillis && now < yEndMillis - 1000) {
                        val elapsedSinceStart = now - yStartMillis
                        val intervalsPassed = elapsedSinceStart / freqMillis
                        val nextRun = yStartMillis + (intervalsPassed + 1) * freqMillis
                        
                        if (nextRun < yEndMillis) {
                            return nextRun
                        }
                    }
                }
            }
        }
        return null
    }
}
