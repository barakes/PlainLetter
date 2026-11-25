pluginManagement {
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
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Tesseract (Hebrew text recognition) is published on JitPack.
        // Only this one library may come from there.
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("cz\\.adaptech.*")
            }
        }
    }
}

rootProject.name = "PlainLetter"
include(":app")
