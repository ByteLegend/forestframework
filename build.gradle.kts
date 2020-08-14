import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm") version "1.3.72"
}

apply(from = "gradle/dependencies.gradle.kts")

val libs: (String) -> String by rootProject.ext

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven { url = URI("https://oss.sonatype.org/content/repositories/snapshots") }
    }

    configureJava()

    if (file("src/main/kotlin").isDirectory || file("src/test/kotlin").isDirectory) {
        configureKotlin()
    }

    if (file("src/test/groovy").isDirectory) {
        configureGroovy()
    }
}

fun Project.configureGroovy() {
    apply(plugin = "groovy")
}

fun Project.configureKotlin() {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "1.8"
    }

    if (file("src/test/kotlin").isDirectory) {
        dependencies {
            testImplementation(libs("kotlinx-coroutines-jdk8"))
            testImplementation(libs("kotlinx-coroutines-core"))
            testImplementation(libs("kotlin-stdlib-jdk8"))
        }
    }
}

fun Project.configureJava() {
    apply(plugin = "java-library")
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.test {
        useJUnitPlatform()
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}