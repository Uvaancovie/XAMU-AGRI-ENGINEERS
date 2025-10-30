package com.example.xamu_wil_project.ui.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.xamu_wil_project.MainActivity
import com.example.xamu_wil_project.cloudinary.ProjectImagesScreen
import com.example.xamu_wil_project.data.Client
import com.example.xamu_wil_project.ui.compose.screens.*
import dagger.hilt.android.EntryPointAccessors

/**
 * Navigation Graph for Xamu Wetlands App
 * Implements all navigation routes with Material 3 + Compose Navigation
 */

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object ClientList : Screen("clients")
    object AddClient : Screen("add_client")
    object EditClient : Screen("edit_client/{clientName}") {
        fun createRoute(clientName: String) = "edit_client/$clientName"
    }
    object ProjectList : Screen("projects/{clientName}") {
        fun createRoute(clientName: String) = "projects/$clientName"
    }
    object AddProject : Screen("add_project/{clientName}") {
        fun createRoute(clientName: String) = "add_project/$clientName"
    }
    object ProjectDetails : Screen("project_details/{companyName}/{projectName}") {
        fun createRoute(companyName: String, projectName: String) =
            "project_details/$companyName/$projectName"
    }
    object AddFieldData : Screen("add_field_data/{companyName}/{projectName}") {
        fun createRoute(companyName: String, projectName: String) =
            "add_field_data/$companyName/$projectName"
    }
    object ViewData : Screen("view_data/{companyName}/{projectName}") {
        fun createRoute(companyName: String, projectName: String) =
            "view_data/$companyName/$projectName"
    }
    object EditBiophysicalData : Screen("edit_biophysical_data/{companyName}/{projectName}/{entryId}") {
        fun createRoute(companyName: String, projectName: String, entryId: String) =
            "edit_biophysical_data/$companyName/$projectName/$entryId"
    }
    object ProjectImages : Screen("project_images/{projectId}") {
        fun createRoute(projectId: String) = "project_images/$projectId"
    }
    object Settings : Screen("settings")
    object Weather : Screen("weather")
}

@Composable
fun XamuNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToClients = { navController.navigate(Screen.ClientList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToWeather = { navController.navigate(Screen.Weather.route) },
                onNavigateToProject = { companyName, projectName ->
                    navController.navigate(
                        Screen.ProjectDetails.createRoute(companyName, projectName)
                    )
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Client List
        composable(Screen.ClientList.route) {
            SelectClientScreen(
                onNavigateBack = { navController.popBackStack() },
                onClientSelected = { client ->
                    navController.navigate(Screen.ProjectList.createRoute(client.companyName ?: ""))
                },
                onAddClient = { navController.navigate(Screen.AddClient.route) },
                onEditClient = { client ->
                    navController.navigate(Screen.EditClient.createRoute(client.companyName ?: ""))
                }
            )
        }

        // Add Client
        composable(Screen.AddClient.route) {
            AddClientScreen(
                onNavigateBack = { navController.popBackStack() },
                onClientSelected = { navController.popBackStack() }
            )
        }

        // Edit Client
        composable(
            route = Screen.EditClient.route,
            arguments = listOf(navArgument("clientName") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientName = backStackEntry.arguments?.getString("clientName") ?: ""
            // You'll need a way to get the client object from the clientName
            // For now, we'll just pass an empty client
            EditClientScreen(
                onNavigateBack = { navController.popBackStack() },
                client = Client(companyName = clientName)
            )
        }

        // Project List
        composable(
            route = Screen.ProjectList.route,
            arguments = listOf(navArgument("clientName") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientName = backStackEntry.arguments?.getString("clientName") ?: ""
            SelectProjectScreen(
                clientName = clientName,
                onNavigateBack = { navController.popBackStack() },
                onProjectSelected = { project ->
                    navController.navigate(
                        Screen.ProjectDetails.createRoute(clientName, project.projectName ?: "")
                    )
                },
                onAddProject = {
                    navController.navigate(Screen.AddProject.createRoute(clientName))
                }
            )
        }

        // Add Project
        composable(
            route = Screen.AddProject.route,
            arguments = listOf(navArgument("clientName") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientName = backStackEntry.arguments?.getString("clientName") ?: ""
            AddProjectScreen(
                onNavigateBack = { navController.popBackStack() },
                onProjectCreated = { project ->
                    navController.popBackStack()
                }
            )
        }

        // Project Details
        composable(
            route = Screen.ProjectDetails.route,
            arguments = listOf(
                navArgument("companyName") { type = NavType.StringType },
                navArgument("projectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val companyName = backStackEntry.arguments?.getString("companyName") ?: ""
            val projectName = backStackEntry.arguments?.getString("projectName") ?: ""
            ProjectDetailsScreen(
                companyName = companyName,
                projectName = projectName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddData = {
                    navController.navigate(
                        Screen.AddFieldData.createRoute(companyName, projectName)
                    )
                },
                onNavigateToViewData = {
                    navController.navigate(
                        Screen.ViewData.createRoute(companyName, projectName)
                    )
                },
                onNavigateToProjectImages = {
                    navController.navigate(Screen.ProjectImages.createRoute(projectName))
                }
            )
        }

        // Add Field Data
        composable(
            route = Screen.AddFieldData.route,
            arguments = listOf(
                navArgument("companyName") { type = NavType.StringType },
                navArgument("projectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val companyName = backStackEntry.arguments?.getString("companyName") ?: ""
            val projectName = backStackEntry.arguments?.getString("projectName") ?: ""
            AddDataToProjectScreen(
                companyName = companyName,
                projectName = projectName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // View Data
        composable(
            route = Screen.ViewData.route,
            arguments = listOf(
                navArgument("companyName") { type = NavType.StringType },
                navArgument("projectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val companyName = backStackEntry.arguments?.getString("companyName") ?: ""
            val projectName = backStackEntry.arguments?.getString("projectName") ?: ""
            ViewDataScreen(
                companyName = companyName,
                projectName = projectName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditEntry = { entryId ->
                    navController.navigate(
                        Screen.EditBiophysicalData.createRoute(companyName, projectName, entryId)
                    )
                }
            )
        }

        // Edit Biophysical Data
        composable(
            route = Screen.EditBiophysicalData.route,
            arguments = listOf(
                navArgument("companyName") { type = NavType.StringType },
                navArgument("projectName") { type = NavType.StringType },
                navArgument("entryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val companyName = backStackEntry.arguments?.getString("companyName") ?: ""
            val projectName = backStackEntry.arguments?.getString("projectName") ?: ""
            val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
            EditBiophysicalDataScreen(
                companyName = companyName,
                projectName = projectName,
                entryId = entryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Project Images
        composable(
            route = Screen.ProjectImages.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectImagesScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Weather
        composable(Screen.Weather.route) {
            WeatherScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
