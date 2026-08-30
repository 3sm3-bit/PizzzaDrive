package com.pizzza.pizzzaDrive.component

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pizzza.pizzzaDrive.ui.AppViewModel
import com.pizzza.pizzzaDrive.ui.splash.SplashScreen
import com.pizzza.pizzzaDrive.ui.driver.ScreenDriverHome

@Composable
fun AppNavigation(
    viewModel: AppViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> {
            SplashScreen(
                onFinished = {
                    navController.navigate(DriverHome) {
                        popUpTo<Splash> { inclusive = true }
                    }
                }
            )
        }
        composable<DriverHome> {
            ScreenDriverHome(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
