package com.example.liteloop.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.*
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.liteloop.data.Task
import com.example.liteloop.presentation.theme.LiteLoopTheme

@WearPreviewDevices
@Composable
fun TaskListPreview() {
    LiteLoopTheme {
        AppScaffold {
            TransformingLazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    ListHeader {
                        Text("LiteLoop Tasks")
                    }
                }
                item {
                    TaskItem(
                        task = Task(1, "Morning Routine", 32400000, 43200000, 10, "1,2,3,4,5", true),
                        onToggle = {},
                        onClick = {}
                    )
                }
                item {
                    TaskItem(
                        task = Task(2, "Afternoon Check", 50400000, 61200000, 30, "1,2,3,4,5", false),
                        onToggle = {},
                        onClick = {}
                    )
                }
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun AddEditTaskPreview() {
    LiteLoopTheme {
        AppScaffold {
            TransformingLazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    ListHeader { Text("Edit Task") }
                }
                item {
                    InputCard(
                        label = "Start: 9h",
                        onIncrement = { },
                        onDecrement = { }
                    )
                }
                item {
                    InputCard(
                        label = "End: 17h",
                        onIncrement = { },
                        onDecrement = { }
                    )
                }
                item {
                    InputCard(
                        label = "Every: 10m",
                        onIncrement = { },
                        onDecrement = { }
                    )
                }
                item {
                    Button(
                        onClick = { },
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
