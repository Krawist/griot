package com.griot

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.ThemeMode
import com.griot.core.design.theme.GriotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
// AppCompat applies the per-app language below Android 13 (GRI-06).
internal class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val dark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            SystemBars(dark = dark)
            GriotTheme(darkTheme = dark, glassBlur = (application as GriotApplication).glassBlur) {
                GriotApp(uiState = uiState, onOnboardingCompleted = viewModel::onOnboardingCompleted)
            }
        }
    }

    // The status and navigation bars draw light icons on a dark theme, dark ones on a light theme.
    @Composable
    private fun SystemBars(dark: Boolean) {
        DisposableEffect(dark) {
            val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
            enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            onDispose {}
        }
    }
}
