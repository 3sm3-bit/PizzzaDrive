package com.pizzza.pizzzaDrive.component

import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pizzza.pizzzaDrive.ui.AppViewModel
import com.pizzza.pizzzaDrive.ui.splash.SplashScreen
import com.pizzza.pizzzaDrive.ui.driver.ScreenDriverHome
import com.pizzza.pizzzaDrive.ui.login.LoginScreen

@Composable
fun AppNavigation(
    viewModel: AppViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> {
            SplashScreen(
                viewModel = viewModel,
                onFinished = {value ->
                    if (value){
                        navController.navigate(DriverHome) {
                            popUpTo<Splash> { inclusive = true }
                        }
                    }else{
                        navController.navigate(Login) {
                            popUpTo<Splash> { inclusive = true }
                        }
                    }

                }
            )
        }
        composable<DriverHome> {
            ScreenDriverHome(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Login) {
                        popUpTo<DriverHome> { inclusive = true }
                    }
                }
            )
        }

        composable<Login> {
            LoginScreen {
                viewModel.checkSession()
                navController.navigate(DriverHome) {
                    popUpTo<Splash> { inclusive = true }
                }
            }
        }

    }
}
