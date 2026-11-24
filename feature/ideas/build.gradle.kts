plugins {
    alias(libs.plugins.krawist.android.feature)
}

android {
    namespace = "com.griot.feature.ideas"
}

dependencies {
    implementation(projects.core.data)

    testImplementation(testFixtures(projects.core.data))
}
