plugins {
    alias(libs.plugins.griot.android.library)
    alias(libs.plugins.griot.android.hilt)
}

android {
    namespace = "com.griot.core.sync"
}

dependencies {
    implementation(projects.core.data)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(testFixtures(projects.core.data))
    testImplementation(libs.kotlinx.coroutines.test)
}
