plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt)
}

// ktlint applies to each module; detekt is configured once for the whole repository.
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt/detekt.yml"))
    source.setFrom(
        files(
            subprojects.flatMap { listOf("${it.projectDir}/src/main/kotlin", "${it.projectDir}/src/test/kotlin") },
        ),
    )
    parallel = true
}

// detekt 1.23.x ships its own Kotlin 2.0.21 compiler: keep Gradle from aligning it with the project's version.
configurations.matching { it.name == "detekt" }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("2.0.21")
        }
    }
}
