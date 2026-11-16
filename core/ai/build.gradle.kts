plugins {
    alias(libs.plugins.griot.android.library)
    alias(libs.plugins.griot.android.hilt)
}

android {
    namespace = "com.griot.core.ai"
}

dependencies {
    implementation(projects.core.phonetics)
    implementation(libs.kotlinx.coroutines.core)
}
