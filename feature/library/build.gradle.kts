plugins {
    alias(libs.plugins.griot.android.feature)
}

android {
    namespace = "com.griot.feature.library"
}

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.data)
    implementation(projects.core.phonetics)

    testImplementation(testFixtures(projects.core.ai))
    testImplementation(testFixtures(projects.core.data))
    testImplementation(libs.mockk)
}
