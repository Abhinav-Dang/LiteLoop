package com.example.liteloop.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.liteloop.data.Task
import com.example.liteloop.receiver.AlarmReceiver
import java.util.*

class ReminderScheduler(private val context: Context?) {

    private val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleNextAlarm(task: Task) {
        if (context == null || alarmManager == null) return
        
        if (!task.isActive) {
            cancelAlarm(task)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        val nextTime = calculateNextOccurrence(task) ?: return

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

    // Extracted for testing
    fun calculateNextOccurrenceAtTime(task: Task, now: Long): Long? {
        val daysList = task.daysOfWeek.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
        if (daysList.isEmpty()) return null

        val today = Calendar.getInstance().apply { timeInMillis = now }
        
        for (i in 0..7) { // Check today and next 7 days
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

                // Handle midnight spanning
                if (task.endTime <= task.startTime) {
                    endCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                if (i == 0) { // Today
                    // If window spans midnight and we are currently in the part of the window 
                    // that belongs to the previous day's start (i.e., after midnight but before end time)
                    // we should check that too.
                    
                    // First check the current day's window
                    if (now < startCal.timeInMillis) {
                        return startCal.timeInMillis
                    } else if (now >= startCal.timeInMillis && now < endCal.timeInMillis - 60000) {
                        val nextFreq = now + (task.frequencyMinutes * 60000)
                        return if (nextFreq < endCal.timeInMillis) nextFreq else null
                    }
                    
                    // Then check if we are in the tail end of yesterday's window
                    if (task.endTime <= task.startTime) {
                        val yesterdayStart = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                        val yesterdayEnd = (endCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
                        
                        if (now >= yesterdayStart.timeInMillis && now < yesterdayEnd.timeInMillis - 60000) {
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
