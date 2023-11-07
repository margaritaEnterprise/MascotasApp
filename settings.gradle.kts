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
        gradlePluginPortal()
        google()
        mavenCentral()
        maven{ url=uri("https://jitpack.io")}
        jcenter(){
            content{
                includeModule("com.theartofdev.edmodo","android-image-cropper")
            }
        }
    }
}

rootProject.name = "MascotasApp"
include(":app")
