package com.example.liteloop.scheduler

import android.content.Context
import android.util.Log
import com.example.liteloop.data.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.*

class OvernightSchedulingTest {

    private fun formatTime(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return "%02d:%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))
    }

    @Test
    fun testOvernightWindow_CurrentTime2334() {
        val task = Task(
            name = "Overnight Task",
            startTime = 22 * 3600000L, // 22:00
            endTime = 4 * 3600000L,   // 04:00
            frequencyMinutes = 5,
            daysOfWeek = "1,2,3,4,5,6,7",
            isActive = true
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 34)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val now = calendar.timeInMillis

        println("DEBUG: Testing Task '${task.name}' [22:00 - 04:00]")
        println("DEBUG: Current simulated time: ${formatTime(now)}")

        val scheduler = ReminderScheduler(null)
        val nextTime = scheduler.calculateNextOccurrenceAtTime(task, now)
        
        assertNotNull("Next occurrence should not be null", nextTime)
        println("DEBUG: Calculated Next Run: ${formatTime(nextTime!!)}")
        
        val resultCal = Calendar.getInstance().apply { timeInMillis = nextTime }
        assertEquals("Hour should be 23", 23, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 39", 39, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun testAnyMinuteInterval_1Min() {
        val task = Task(
            name = "1 Min Task",
            startTime = 9 * 3600000L,
            endTime = 17 * 3600000L,
            frequencyMinutes = 1,
            daysOfWeek = "1,2,3,4,5,6,7",
            isActive = true
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 10)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val now = calendar.timeInMillis

        println("DEBUG: Testing 1-minute interval at 10:00:00")
        val scheduler = ReminderScheduler(null)
        val nextTime = scheduler.calculateNextOccurrenceAtTime(task, now)
        
        assertNotNull(nextTime)
        println("DEBUG: Calculated Next Run (1m): ${formatTime(nextTime!!)}")
        
        val resultCal = Calendar.getInstance().apply { timeInMillis = nextTime }
        assertEquals(10, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(1, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun testAnyMinuteInterval_3Min() {
        val task = Task(
            name = "3 Min Task",
            startTime = 9 * 3600000L,
            endTime = 17 * 3600000L,
            frequencyMinutes = 3,
            daysOfWeek = "1,2,3,4,5,6,7",
            isActive = true
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 10)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val now = calendar.timeInMillis

        println("DEBUG: Testing 3-minute interval at 10:00:00")
        val scheduler = ReminderScheduler(null)
        val nextTime = scheduler.calculateNextOccurrenceAtTime(task, now)
        
        assertNotNull(nextTime)
        println("DEBUG: Calculated Next Run (3m): ${formatTime(nextTime!!)}")
        
        val resultCal = Calendar.getInstance().apply { timeInMillis = nextTime }
        assertEquals(10, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(3, resultCal.get(Calendar.MINUTE))
    }
}
