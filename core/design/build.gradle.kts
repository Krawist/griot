plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.compose)
}

android {
    namespace = "com.griot.core.design"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    // Glass surfaces and backdrop of the kit, used by every screen.
    api("io.github.krawist:design")
}
