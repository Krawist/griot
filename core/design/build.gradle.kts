plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.compose)
}

android {
    namespace = "com.griot.core.design"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.haze)
    implementation(libs.haze.blur)
}
