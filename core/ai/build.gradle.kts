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
    implementation(projects.core.data)
    implementation(projects.core.phonetics)
    implementation(libs.anthropic.java)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.mlkit.genai.prompt)

    testFixturesImplementation(libs.kotlinx.coroutines.core)

    testImplementation(testFixtures(projects.core.data))
    testImplementation(libs.kotlinx.coroutines.test)
}
