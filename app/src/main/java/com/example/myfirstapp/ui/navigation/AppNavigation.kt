package com.example.myfirstapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myfirstapp.domain.repository.NoteRepository
import com.example.myfirstapp.ui.screens.detail.NoteDetailScreen
import com.example.myfirstapp.ui.screens.detail.NoteDetailViewModel
import com.example.myfirstapp.ui.screens.home.HomeScreen
import com.example.myfirstapp.ui.screens.home.HomeViewModel
import com.example.myfirstapp.ui.screens.record.RecordScreen
import com.example.myfirstapp.ui.screens.record.RecordViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Record : Screen("record")
    data object Detail : Screen("detail/{noteId}") {
        fun createRoute(noteId: String) = "detail/$noteId"
    }
}

@Composable
fun AppNavigation(
    repository: NoteRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel { HomeViewModel(repository) }
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToRecord = { navController.navigate(Screen.Record.route) },
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(Screen.Record.route) {
            val recordViewModel: RecordViewModel = viewModel { RecordViewModel(repository) }
            RecordScreen(
                viewModel = recordViewModel,
                onBackClick = { navController.popBackStack() },
                onRecordingCompleted = { newNoteId ->
                    navController.popBackStack()
                    navController.navigate(Screen.Detail.createRoute(newNoteId))
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            val detailViewModel: NoteDetailViewModel = viewModel(key = noteId) { NoteDetailViewModel(repository) }
            NoteDetailScreen(
                noteId = noteId,
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
