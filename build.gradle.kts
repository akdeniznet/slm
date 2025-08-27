import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import com.android.build.gradle.BaseExtension

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("com.github.recloudstream:gradle:-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")

    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.cloudstream(configuration: CloudstreamExtension.() -> Unit) = extensions.getByName<CloudstreamExtension>("cloudstream").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "kotlin-android")
    apply(plugin = "com.lagradost.cloudstream3.gradle")

    cloudstream {
        setRepo(System.getenv("GITHUB_REPOSITORY") ?: "https://github.com/keyiflerolsun/Kekik-cloudstream")
        authors = listOf("keyiflerolsun")
    }

    android {
         namespace = "com.keyiflerolsun"  // Bu satırı ekleyin
    
        
        defaultConfig {
            minSdk = 26
            compileSdkVersion(33)
            targetSdk = 33
            
            multiDexEnabled = true
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xno-call-assertions",
                    "-Xno-param-assertions",
                    "-Xno-receiver-assertions"
                )
            }
        }
    }

    dependencies {
        val apk by configurations
        val implementation by configurations

        apk("com.lagradost:cloudstream3:pre-release")
        implementation("androidx.multidex:multidex:2.0.1")
        implementation(kotlin("stdlib"))
        implementation("com.github.Blatzar:NiceHttp:0.4.4")
        implementation("org.jsoup:jsoup:1.17.2")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.0")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}