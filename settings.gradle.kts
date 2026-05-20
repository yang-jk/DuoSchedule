pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google") {
            name = "Aliyun Google"
        }
        maven("https://maven.aliyun.com/repository/central") {
            name = "Aliyun Central"
        }
        maven("https://maven.aliyun.com/repository/gradle-plugin") {
            name = "Aliyun Gradle Plugin"
        }
        maven("https://maven.aliyun.com/repository/public") {
            name = "Aliyun Public"
        }
        google()
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
        maven("https://maven.aliyun.com/repository/google") {
            name = "Aliyun Google"
        }
        maven("https://maven.aliyun.com/repository/central") {
            name = "Aliyun Central"
        }
        maven("https://maven.aliyun.com/repository/public") {
            name = "Aliyun Public"
        }
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "DuoSchedule"
include(":app")
