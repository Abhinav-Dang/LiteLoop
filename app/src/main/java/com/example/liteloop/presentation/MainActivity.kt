package com.example.liteloop.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.liteloop.presentation.theme.LiteLoopTheme
import com.example.liteloop.presentation.viewmodel.TaskViewModel
import kotlinx.coroutines.runBlocking
import com.example.liteloop.data.AppDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiteLoopApp()
        }
    }
}

@Composable
fun LiteLoopApp() {
    val navController = rememberSwipeDismissableNavController()
    val viewModel: TaskViewModel = viewModel()

    LiteLoopTheme {
        AppScaffold {
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "task_list"
            ) {
                composable("task_list") {
                    TaskListScreen(
                        viewModel = viewModel,
                        onAddTask = { navController.navigate("add_edit_task") },
                        onEditTask = { task ->
                            navController.navigate("add_edit_task?taskId=${task.id}")
                        }
                    )
                }
                composable(
                    route = "add_edit_task?taskId={taskId}",
                    arguments = listOf(
                        navArgument("taskId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
                    val task = if (taskId != -1L) {
                        val db = AppDatabase.getDatabase(navController.context)
                        runBlocking { db.taskDao().getTaskById(taskId) }
                    } else null

                    AddEditTaskScreen(
                        viewModel = viewModel,
                        task = task,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
