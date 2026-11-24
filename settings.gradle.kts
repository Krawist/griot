pluginManagement {
    includeBuild("../Kit/build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    // Versions shared by all the apps, in the kit checked out next to this repository.
    versionCatalogs {
        create("libs") {
            from(files("../Kit/gradle/libs.versions.toml"))
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "griot"

// Shared modules of the kit, such as its detekt rules.
includeBuild("../Kit")


include(":app")
include(":feature:onboarding")
include(":feature:library")
include(":feature:editor")
include(":feature:ideas")
include(":feature:settings")
include(":core:design")
include(":core:data")
include(":core:phonetics")
include(":core:sync")
include(":core:ai")
