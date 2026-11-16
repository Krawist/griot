plugins {
    alias(libs.plugins.griot.android.library)
    alias(libs.plugins.griot.android.hilt)
    alias(libs.plugins.griot.android.room)
}

android {
    namespace = "com.griot.core.data"

    // Fakes shared by the tests of the modules that use this one.
    testFixtures.enable = true
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(libs.androidx.appcompat)
    implementation(libs.mlkit.language.id)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.sqlite.bundled.jvm)
}
