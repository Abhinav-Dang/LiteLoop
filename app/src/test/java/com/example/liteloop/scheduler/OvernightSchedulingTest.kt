package com.example.liteloop.scheduler

import com.example.liteloop.data.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class OvernightSchedulingTest {

    private fun formatTime(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return "%02d:%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))
    }

    @Test
    fun testOvernightWindow_AnchoredInterval() {
        val task = Task(
            name = "Overnight Task",
            startTime = 22 * 3600000L, // 22:00
            endTime = 4 * 3600000L,   // 04:00
            frequencyMinutes = 5,
            daysOfWeek = "1,2,3,4,5,6,7",
            isActive = true
        )

        // Simulated current time: 23:34:00
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 34)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val now = calendar.timeInMillis

        println("DEBUG: Testing Anchored Logic for '${task.name}'")
        println("DEBUG: Current simulated time: ${formatTime(now)}")

        val scheduler = ReminderScheduler(null)
        val nextTime = scheduler.calculateNextOccurrenceAtTime(task, now)
        
        assertNotNull("Next occurrence should not be null", nextTime)
        println("DEBUG: Calculated Next Run: ${formatTime(nextTime!!)}")
        
        val resultCal = Calendar.getInstance().apply { timeInMillis = nextTime }
        
        // Anchored to 22:00. Intervals: 22:05, 22:10 ... 23:30, 23:35.
        // So at 23:34:00, the next run should be 23:35:00.
        assertEquals("Hour should be 23", 23, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 35", 35, resultCal.get(Calendar.MINUTE))
        assertTrue("Next time must be in future", nextTime > now)
    }

    @Test
    fun testStartOfWindow_TriggerExactlyAtStart() {
        val task = Task(
            name = "Morning Task",
            startTime = 9 * 3600000L, // 09:00
            endTime = 12 * 3600000L,
            frequencyMinutes = 10,
            daysOfWeek = "1,2,3,4,5,6,7",
            isActive = true
        )

        // Current time: 08:30
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 30)
        calendar.set(Calendar.SECOND, 0)
        val now = calendar.timeInMillis

        val scheduler = ReminderScheduler(null)
        val nextTime = scheduler.calculateNextOccurrenceAtTime(task, now)
        
        assertNotNull(nextTime)
        val resultCal = Calendar.getInstance().apply { timeInMillis = nextTime!! }
        
        // Should trigger at exactly the start time
        assertEquals(9, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun testNearEndOfWindow_NoMoreIntervals() {
        val task = Task(
            name = "Morning Task",
            startTime = 9 * 3600000L, // 09:00
            endTime = 9 * 3600000L + 15 * 60000L, // 09:15
            frequencyMinutes = 10,
            daysOfWeek = "1,2,3,4,5,6,7",
            isActive = true
        )

        // Current time: 09:12
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 12)
        calendar.set(Calendar.SECOND, 0)
        val now = calendar.timeInMillis

        val scheduler = ReminderScheduler(null)
        val nextTime = scheduler.calculateNextOccurrenceAtTime(task, now)
        
        // 09:00 was first trigger. 09:10 was second. Next would be 09:20.
        // But 09:20 is past 09:15.
        // So it should schedule for the NEXT DAY at 09:00.
        
        assertNotNull(nextTime)
        val resultTime = nextTime!!
        val resultCal = Calendar.getInstance().apply { timeInMillis = resultTime }
        assertEquals(9, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
        assertTrue("Must be at least 12 hours in future (tomorrow)", resultTime - now > 12 * 3600000)
    }
}
