package com.karthicbz.klauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.karthicbz.klauncher.repository.ThemeRepository
import com.karthicbz.klauncher.ui.home.HomeScreen
import com.karthicbz.klauncher.ui.navigation.Screen
import com.karthicbz.klauncher.ui.settings.SettingsScreen
import com.karthicbz.klauncher.ui.theme.KlauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeRepository: ThemeRepository

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentTheme by themeRepository.currentTheme.collectAsState()
            val navController = rememberNavController()

            KlauncherTheme(config = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
