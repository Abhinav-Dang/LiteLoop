package com.example.liteloop.presentation.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.liteloop.data.AppDatabase
import com.example.liteloop.data.Task
import com.example.liteloop.scheduler.ReminderScheduler
import com.example.liteloop.service.LiteLoopService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = AppDatabase.getDatabase(application).taskDao()
    private val scheduler = ReminderScheduler(application)

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    init {
        // Automatically start/stop service based on active tasks
        viewModelScope.launch {
            allTasks
                .map { tasks -> tasks.any { it.isActive } }
                .distinctUntilChanged()
                .collect { hasActiveTasks ->
                    updateServiceState(hasActiveTasks)
                }
        }
    }

    private fun updateServiceState(shouldRun: Boolean) {
        val intent = Intent(getApplication(), LiteLoopService::class.java)
        if (shouldRun) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplication<Application>().startForegroundService(intent)
            } else {
                getApplication<Application>().startService(intent)
            }
        } else {
            getApplication<Application>().stopService(intent)
        }
    }

    fun isAlarmPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        return true
    }

    fun insertTask(task: Task) {
        viewModelScope.launch {
            val id = taskDao.insertTask(task)
            scheduler.scheduleNextAlarm(task.copy(id = id))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task)
            if (task.isActive) {
                scheduler.scheduleNextAlarm(task)
            } else {
                scheduler.cancelAlarm(task)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            scheduler.cancelAlarm(task)
            taskDao.deleteTask(task)
        }
    }
    
    fun toggleTaskActive(task: Task) {
        val updatedTask = task.copy(isActive = !task.isActive)
        updateTask(updatedTask)
    }
}
