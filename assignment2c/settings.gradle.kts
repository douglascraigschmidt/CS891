pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

include(":eureka")
include(":gateway")
include(":client")
include(":movies-ex")
include(":database")
include(":database-ex")
include(":timer")
include(":testing")
include(":common")
