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
    val vertxVersion = "4.0.0-milestone5"
    val kotlinVersion = "1.3.72"
    val kotlinxVersion = "1.3.7"
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    api("io.vertx:vertx-lang-kotlin:$vertxVersion")
    api("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    api(project(":core"))
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "junit-vintage")
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}