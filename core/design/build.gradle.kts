plugins {
    alias(libs.plugins.griot.android.library)
    alias(libs.plugins.griot.android.compose)
}

android {
    namespace = "com.griot.core.design"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.haze)
    implementation(libs.haze.blur)
}
