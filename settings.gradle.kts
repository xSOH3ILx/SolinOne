pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SolinOne"
include(":app")
include(":core:ui")
include(":core:calendar")
include(":core:database")
include(":core:security")
include(":features:finance")
include(":features:calendar")
