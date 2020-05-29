import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    jcenter()
    mavenCentral()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    val kotlinVersion = "1.3.72"
    val vertxVersion = "4.0.0-milestone5"
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.7")
    api("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    api("io.vertx:vertx-core:$vertxVersion")
}
