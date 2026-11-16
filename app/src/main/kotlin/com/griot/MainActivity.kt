package com.griot

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            GriotTheme(glassBlur = (application as GriotApplication).glassBlur) {
                GriotApp(uiState = uiState, onOnboardingCompleted = viewModel::onOnboardingCompleted)
            }
        }
    }
}
