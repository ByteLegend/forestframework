import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("application")
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
    val jpaVersion = "2.2"
    val javaxValidationVersion = "2.0.1.Final"
    implementation(project(":core"))
    implementation("javax.ws.rs:jsr311-api:1.1.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("javax.persistence:javax.persistence-api:$jpaVersion")
    implementation("javax.validation:validation-api:$javaxValidationVersion")
}

application {
    mainClassName = "io.forestframework.samples.petclinic.PetClinicApplicationKt"
}