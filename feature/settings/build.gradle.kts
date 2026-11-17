plugins {
    alias(libs.plugins.griot.android.feature)
}

android {
    namespace = "com.griot.feature.settings"
}

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.data)

    testImplementation(testFixtures(projects.core.ai))
    testImplementation(testFixtures(projects.core.data))
}
