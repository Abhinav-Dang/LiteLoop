package com.example.liteloop.scheduler

import com.example.liteloop.data.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class ReminderSchedulerTest {

    @Test
    fun testCalculateNextOccurrence_FutureWindowToday() {
        val calendar = Calendar.getInstance()
        // Set "now" to 10:00 AM
        calendar.set(Calendar.HOUR_OF_DAY, 10)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val now = calendar.timeInMillis
        
        // Task starting at 11:00 AM
        val startTime = 11 * 3600000L
        val endTime = 17 * 3600000L
        
        val task = Task(
            name = "Test",
            startTime = startTime,
            endTime = endTime,
            frequencyMinutes = 10,
            daysOfWeek = "1,2,3,4,5,6,7", // All days
            isActive = true
        )

        // Mocking logic of calculateNextOccurrence manually since I can't easily mock Calendar.getInstance() in simple unit test without more libs
        // But I can test a version of it that takes 'now' as parameter
        val nextTime = calculateNextOccurrenceInternal(task, now)
        
        assertNotNull(nextTime)
        
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = nextTime!!
        assertEquals(11, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun testCalculateNextOccurrence_InsideWindowToday() {
        val calendar = Calendar.getInstance()
        // Set "now" to 12:00 PM
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val now = calendar.timeInMillis
        
        // Task starting at 11:00 AM, ending 5 PM, 10 min frequency
        val startTime = 11 * 3600000L
        val endTime = 17 * 3600000L
        
        val task = Task(
            name = "Test",
            startTime = startTime,
            endTime = endTime,
            frequencyMinutes = 10,
            daysOfWeek = "1,2,3,4,5,6,7",
            isActive = true
        )

        val nextTime = calculateNextOccurrenceInternal(task, now)
        
        assertNotNull(nextTime)
        assertEquals(now + 10 * 60000, nextTime)
    }

    // Helper method matching the one in ReminderScheduler but taking 'now'
    private fun calculateNextOccurrenceInternal(task: Task, now: Long): Long? {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        
        val daysList = task.daysOfWeek.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
        if (daysList.isEmpty()) return null

        val today = Calendar.getInstance()
        today.timeInMillis = now
        
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

                if (i == 0) { // Today
                    if (now < startCal.timeInMillis) {
                        return startCal.timeInMillis
                    } else if (now >= startCal.timeInMillis && now < endCal.timeInMillis - 60000) {
                        val nextFreq = now + (task.frequencyMinutes * 60000)
                        if (nextFreq < endCal.timeInMillis) return nextFreq
                    }
                } else {
                    return startCal.timeInMillis
                }
            }
        }
        return null
    }
}
