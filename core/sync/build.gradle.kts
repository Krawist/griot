plugins {
    alias(libs.plugins.krawist.android.library)
    alias(libs.plugins.krawist.android.hilt)
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
