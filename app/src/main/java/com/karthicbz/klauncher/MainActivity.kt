package com.karthicbz.klauncher

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import coil.compose.rememberAsyncImagePainter
import com.karthicbz.klauncher.repository.ThemeRepository
import com.karthicbz.klauncher.repository.UserPreferencesRepository
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

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentTheme by themeRepository.currentTheme.collectAsState()
            val wallpaperColor by userPreferencesRepository.wallpaperColor.collectAsState()
            val wallpaperImageUrl by userPreferencesRepository.wallpaperImageUrl.collectAsState()
            val navController = rememberNavController()

            KlauncherTheme(config = currentTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (wallpaperImageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = if (wallpaperImageUrl!!.startsWith("content://"))
                                    Uri.parse(wallpaperImageUrl)
                                else wallpaperImageUrl
                            ),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        tonalElevation = 0.dp,
                        colors = SurfaceDefaults.colors(
                            containerColor = if (wallpaperImageUrl != null) {
                                androidx.compose.ui.graphics.Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    wallpaperColor?.let {
                                        runCatching {
                                            Modifier.background(
                                                androidx.compose.ui.graphics.Color(
                                                    android.graphics.Color.parseColor(it)
                                                )
                                            )
                                        }.getOrNull()
                                    } ?: Modifier
                                )
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
    }
}
