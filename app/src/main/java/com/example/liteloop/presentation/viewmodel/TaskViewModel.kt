package com.example.liteloop.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.liteloop.data.AppDatabase
import com.example.liteloop.data.Task
import com.example.liteloop.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = AppDatabase.getDatabase(application).taskDao()
    private val scheduler = ReminderScheduler(application)

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

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
