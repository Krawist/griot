plugins {
    alias(libs.plugins.griot.android.feature)
}

android {
    namespace = "com.griot.feature.editor"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.phonetics)

    testImplementation(testFixtures(projects.core.data))
    testImplementation(libs.mockk)
}
