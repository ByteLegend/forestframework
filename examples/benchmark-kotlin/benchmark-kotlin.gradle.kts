import nu.studer.gradle.rocker.RockerConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("application")
    id("nu.studer.rocker") version "1.0.1"
}

repositories {
    jcenter()
    mavenCentral()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

sourceSets.getByName("main").java {
    srcDir("src/rocker")
}

dependencies {
    val kotlinVersion = "1.3.72"
    val jpaVersion = "2.2"
    val javaxValidationVersion = "2.0.1.Final"
    val vertxVersion = "4.0.0.Beta1"
    val jacksonVersion = "2.10.3"
    val mysqlDriverVersion = "8.0.20"
    implementation(project(":extensions:pg-client-extension"))
//    implementation(project(":redis-client-extension"))
//    implementation(project(":cors-extension"))
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
//    implementation("io.vertx:vertx-jdbc-client:$vertxVersion")
    implementation(project(":core"))
//    implementation("javax.ws.rs:jsr311-api:1.1.1")
//    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
//    implementation("javax.persistence:javax.persistence-api:$jpaVersion")
//    implementation("javax.validation:validation-api:$javaxValidationVersion")
//    implementation("ch.qos.logback:logback-classic:$logbackVersion")
//    implementation("mysql:mysql-connector-java:$mysqlDriverVersion")

    val nettyVersion = "4.1.50.Final"
    val rockerVersion = "1.3.0"
//    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.netty:netty-transport-native-kqueue:$nettyVersion:osx-x86_64")
    implementation("io.netty:netty-transport-native-epoll:$nettyVersion:linux-x86_64")
    implementation("com.fizzed:rocker-compiler:$rockerVersion")
}

application {
    mainClassName = "io.forestframework.benchmark.AppKt"
}

configure<NamedDomainObjectContainer<RockerConfig>>() {
    create("main") {
        setTemplateDir(file("src/main/resources/templates"))
        setOutputDir(file("src/main/rocker"))
        setOptimize(true)
    }
}

extra["rockerVersion"] = "1.3.0"

//rocker {
//    main {
//        templateDir = file("src/resources/templates")
//        outputDir = file("src/generated/rocker")
//        optimize = true  // optional
//    }
//}

// Template can contain unused imports
tasks.named("checkstyleMain").configure { enabled = false }