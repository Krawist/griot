package com.griot

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.krawist.design.glass.supportsGlassBlur

@HiltAndroidApp
internal class GriotApplication : Application() {
    /** Whether glass surfaces blur, chosen once per process, never per frame or per screen. */
    val glassBlur: Boolean by lazy { supportsGlassBlur() }
}
