import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//
plugins {
    kotlin("jvm") version "1.3.72"
}

apply(from = "gradle/dependencies.gradle.kts")

allprojects {
    repositories {
        jcenter()
        mavenCentral()
    }

    configureJavaProject()

    if (file("src/main/kotlin").isDirectory || file("src/test/kotlin").isDirectory) {
        configureKotlinProject()
    }
}

fun Project.configureKotlinProject() {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "1.8"
    }
}

fun Project.configureJavaProject() {
    apply(plugin = "java-library")
    tasks.test {
        useJUnitPlatform()
    }
}
