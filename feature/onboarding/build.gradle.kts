plugins {
    alias(libs.plugins.griot.android.feature)
}

android {
    namespace = "com.griot.feature.onboarding"
}

dependencies {
    implementation(projects.core.data)

    testImplementation(testFixtures(projects.core.data))
}
