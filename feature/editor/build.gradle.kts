plugins {
    alias(libs.plugins.krawist.android.feature)
}

android {
    namespace = "com.griot.feature.editor"
}

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.data)
    implementation(projects.core.phonetics)

    testImplementation(testFixtures(projects.core.ai))
    testImplementation(testFixtures(projects.core.data))
    testImplementation(libs.mockk)
}
