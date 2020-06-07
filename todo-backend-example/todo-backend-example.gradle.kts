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
    val logbackVersion = "1.2.3"
    val vertxVersion = "4.0.0-milestone5"
    val jacksonVersion = "2.10.3"
    implementation(project(":jdbc-client-extension"))
    implementation(project(":redis-client-extension"))
    implementation(project(":cors-extension"))
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-jdbc-client:$vertxVersion")
    implementation(project(":core"))
    implementation("javax.ws.rs:jsr311-api:1.1.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("javax.persistence:javax.persistence-api:$jpaVersion")
    implementation("javax.validation:validation-api:$javaxValidationVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}

application {
    mainClassName = "org.forestframework.ToDoApplicationKt"
}