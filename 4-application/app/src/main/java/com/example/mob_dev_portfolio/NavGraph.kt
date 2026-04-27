package com.example.mob_dev_portfolio

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val viewModel: QuoteViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onProfileSelected = { profileId ->
                    navController.navigate("quotes/$profileId")
                },
                onImportClicked = {
                    navController.navigate("quotes/-1")
                }
            )
        }

        composable(
            route = "quotes/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.IntType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getInt("profileId") ?: -1
            QuoteListScreen(
                profileId = profileId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}