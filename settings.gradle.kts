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
    plugins {
        id("co.uzzu.dotenv.gradle") version "4.0.0"
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
    }
}

rootProject.name = "Paysmart"
include(":app")
include(":core:common")
include(":core:navigation")
include(":core:ui")
include(":core:security")
include(":core:database")
include(":core:firebase")
include(":data:auth")
include(":data:user")
include(":data:wallet")
include(":data:invoice")
include(":data:notifications")
include(":feature:account")
include(":feature:profile")
include(":feature:home")
include(":feature:wallet")
include(":core:invoice-models")
include(":core:models")
include(":sandbox")
