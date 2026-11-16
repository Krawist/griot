plugins {
    alias(libs.plugins.griot.android.feature)
}

android {
    namespace = "com.griot.feature.settings"
}

dependencies {
    implementation(projects.core.data)

    testImplementation(testFixtures(projects.core.data))
}
