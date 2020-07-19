import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    api(project(":junit5-extension"))
    api(project(":core-kotlin"))
    api("io.vertx:vertx-web-client:4.0.0-milestone5")
}