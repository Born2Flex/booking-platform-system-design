pluginManagement {
    // Convention plugins (shared build config) live in an included build.
    includeBuild("build-logic")
}

plugins {
    // Lets Gradle auto-provision the JDK 25 toolchain if it isn't installed locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "booking-platform"

include(
    ":services:event-catalog-service",
)
