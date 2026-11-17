plugins {
    alias(libs.plugins.griot.android.library)
    alias(libs.plugins.griot.android.hilt)
}

android {
    namespace = "com.griot.core.ai"

    // Fakes shared by the tests of the modules that use this one.
    testFixtures.enable = true
}

dependencies {
    implementation(projects.core.phonetics)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
