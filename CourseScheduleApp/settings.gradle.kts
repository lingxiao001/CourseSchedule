pluginManagement {
    repositories {
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }
        maven { setUrl("https://mirrors.tuna.tsinghua.edu.cn/maven/") }
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }
        maven { setUrl("https://mirrors.tuna.tsinghua.edu.cn/maven/") }
        mavenCentral()
        google()
    }
}

rootProject.name = "CourseScheduleApp"
include(":app")