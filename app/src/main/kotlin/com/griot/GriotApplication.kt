package com.griot

import android.app.Application
import com.griot.core.design.glass.supportsGlassBlur
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
internal class GriotApplication : Application() {
    /** Whether glass surfaces blur, chosen once per process, never per frame or per screen. */
    val glassBlur: Boolean by lazy { supportsGlassBlur() }
}
