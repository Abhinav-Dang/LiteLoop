package com.example.liteloop.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import com.example.liteloop.data.Task
import com.example.liteloop.presentation.viewmodel.TaskViewModel

@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    task: Task?,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(task?.name ?: "") }
    
    var startHours by remember { mutableStateOf((task?.startTime ?: 32400000) / 3600000) }
    var startMinutes by remember { mutableStateOf(((task?.startTime ?: 32400000) % 3600000) / 60000) }
    
    var endHours by remember { mutableStateOf((task?.endTime ?: 61200000) / 3600000) }
    var endMinutes by remember { mutableStateOf(((task?.endTime ?: 61200000) % 3600000) / 60000) }
    
    var frequency by remember { mutableStateOf(task?.frequencyMinutes ?: 10) }
    var isTtsEnabled by remember { mutableStateOf(task?.isTtsEnabled ?: true) }
    
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(onClick = {
                val newTask = Task(
                    id = task?.id ?: 0,
                    name = name.ifEmpty { "Reminder" },
                    startTime = startHours * 3600000 + startMinutes * 60000,
                    endTime = endHours * 3600000 + endMinutes * 60000,
                    frequencyMinutes = frequency,
                    daysOfWeek = "1,2,3,4,5,6,7",
                    isActive = task?.isActive ?: true,
                    isTtsEnabled = isTtsEnabled
                )
                if (task == null) viewModel.insertTask(newTask) else viewModel.updateTask(newTask)
                onBack()
            }) {
                Text("Save")
            }
        }
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ListHeader { Text(if (task == null) "Add Task" else "Edit Task") }
            }
            
            item {
                Card(onClick = {}, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text("Task Name", style = MaterialTheme.typography.labelSmall)
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    }
                }
            }
            
            item {
                TimeInputCard(
                    label = "Start Time",
                    hours = startHours,
                    minutes = startMinutes,
                    onHoursChange = { startHours = it },
                    onMinutesChange = { startMinutes = it }
                )
            }

            item {
                TimeInputCard(
                    label = "End Time",
                    hours = endHours,
                    minutes = endMinutes,
                    onHoursChange = { endHours = it },
                    onMinutesChange = { endMinutes = it }
                )
            }

            item {
                InputCard(
                    label = "Every: ${frequency}m",
                    onIncrement = { if (frequency < 120) frequency += 1 },
                    onDecrement = { if (frequency > 1) frequency -= 1 }
                )
            }

            item {
                Card(
                    onClick = { isTtsEnabled = !isTtsEnabled },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Name", style = MaterialTheme.typography.bodySmall)
                        Button(
                            onClick = { isTtsEnabled = !isTtsEnabled },
                            modifier = Modifier.size(48.dp),
                            colors = if (isTtsEnabled) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text(if (isTtsEnabled) "On" else "Off", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (task != null) {
                item {
                    Button(
                        onClick = {
                            viewModel.deleteTask(task)
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun TimeInputCard(
    label: String,
    hours: Long,
    minutes: Long,
    onHoursChange: (Long) -> Unit,
    onMinutesChange: (Long) -> Unit
) {
    Card(onClick = {}, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Column(modifier = Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { if (hours < 23) onHoursChange(hours + 1) else onHoursChange(0) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add Hour")
                    }
                    Text("%02d".format(hours), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { if (hours > 0) onHoursChange(hours - 1) else onHoursChange(23) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Remove Hour")
                    }
                }
                
                Text(":", style = MaterialTheme.typography.titleMedium)
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { if (minutes < 59) onMinutesChange(minutes + 1) else onMinutesChange(0) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add Minute")
                    }
                    Text("%02d".format(minutes), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { if (minutes > 0) onMinutesChange(minutes - 1) else onMinutesChange(59) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Remove Minute")
                    }
                }
            }
        }
    }
}

@Composable
fun InputCard(
    label: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(onClick = {}, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Row {
                IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
    }
}
