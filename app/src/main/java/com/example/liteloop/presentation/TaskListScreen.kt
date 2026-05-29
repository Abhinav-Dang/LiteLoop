package com.example.liteloop.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.example.liteloop.data.Task
import com.example.liteloop.presentation.viewmodel.TaskViewModel
import com.example.liteloop.scheduler.ReminderScheduler
import java.util.Calendar

@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState(initial = emptyList())
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(onClick = onAddTask) {
                Text("Add Task")
            }
        }
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ListHeader {
                    Text("LiteLoop Tasks")
                }
            }

            if (tasks.isEmpty()) {
                item {
                    Text(
                        text = "No tasks yet",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskActive(task) },
                        onClick = { onEditTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val scheduler = remember { ReminderScheduler(context) }
    val nextRunMillis = remember(task) { 
        if (task.isActive) scheduler.calculateNextOccurrenceAtTime(task, System.currentTimeMillis()) else null 
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatTime(task.startTime)} - ${formatTime(task.endTime)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Every ${task.frequencyMinutes}m",
                    style = MaterialTheme.typography.bodySmall
                )
                
                if (task.isActive) {
                    Text(
                        text = "NR: ${nextRunMillis?.let { formatTime(it) } ?: "Stopped"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Button(
                onClick = onToggle,
                modifier = Modifier.size(48.dp),
                colors = if (task.isActive) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
            ) {
                Text(if (task.isActive) "On" else "Off", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)
    return "%02d:%02d".format(hours, minutes)
}
