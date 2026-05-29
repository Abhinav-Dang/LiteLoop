package com.example.liteloop.scheduler

import android.content.Context
import com.example.liteloop.data.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.*

class OvernightSchedulingTest {

    @Test
    fun testOvernightWindow_CurrentTime2334() {
        // Setup Task: 22:00 (10 PM) to 04:00 (4 AM), 5 min frequency
        val task = Task(
            name = "Overnight Task",
            startTime = 22 * 3600000L, // 22:00
            endTime = 4 * 3600000L,   // 04:00
            frequencyMinutes = 5,
            daysOfWeek = "1,2,3,4,5,6,7",
            isActive = true
        )

        // Current time: 23:34
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 34)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val now = calendar.timeInMillis

        val context = mock(Context::class.java)
        val scheduler = ReminderScheduler(context)
        
        val nextTime = scheduler.calculateNextOccurrenceAtTime(task, now)
        
        assertNotNull("Next occurrence should not be null", nextTime)
        
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = nextTime!!
        
        // Expected: 23:34 + 5 mins = 23:39
        assertEquals("Hour should be 23", 23, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 39", 39, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun testOvernightWindow_NearEndAt0359() {
        // Setup Task: 22:00 to 04:00, 5 min frequency
        val task = Task(
            name = "Overnight Task",
            startTime = 22 * 3600000L,
            endTime = 4 * 3600000L,
            frequencyMinutes = 5,
            daysOfWeek = "1,2,3,4,5,6,7",
            isActive = true
        )

        // Current time: 03:59 (Next Day)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 3)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val now = calendar.timeInMillis

        val context = mock(Context::class.java)
        val scheduler = ReminderScheduler(context)
        
        val nextTime = scheduler.calculateNextOccurrenceAtTime(task, now)
        
        // Since 03:59 + 5 mins = 04:04 which is past 04:00, it should return null for today
        // OR return the window start for the next day. 
        // In our current implementation, it checks 0..7 days.
        
        assertNotNull("Next occurrence should not be null (should find next day's window)", nextTime)
        
        val resultCal = Calendar.getInstance()
        resultCal.timeInMillis = nextTime!!
        
        // Expected: Start of next day's window (22:00)
        assertEquals("Hour should be 22 (next day start)", 22, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 0", 0, resultCal.get(Calendar.MINUTE))
        
        // Verify it is indeed a future time
        assertTrue("Next time should be in the future", nextTime > now)
    }
    
    private fun assertTrue(message: String, condition: Boolean) {
        org.junit.Assert.assertTrue(message, condition)
    }
}
