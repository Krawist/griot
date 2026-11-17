plugins {
    alias(libs.plugins.griot.android.library)
    alias(libs.plugins.griot.android.hilt)
    alias(libs.plugins.griot.android.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.griot.core.data"

    // Fakes shared by the tests of the modules that use this one.
    testFixtures.enable = true
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(projects.core.phonetics)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.palette)
    implementation(libs.mlkit.language.id)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.sqlite.bundled.jvm)
}
