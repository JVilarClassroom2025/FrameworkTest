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
        // jitpack
        maven("https://jitpack.io")
    }
}

rootProject.name="Framework Test"
include(":app")
// include(":vj1229framework")
