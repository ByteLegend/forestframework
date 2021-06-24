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
    val vertxVersion = "4.1.0"
    implementation(project(":extensions:pg-client-extension"))
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation(project(":core"))

    val nettyVersion = "4.1.50.Final"
    val rockerVersion = "1.3.0"
    implementation("io.netty:netty-transport-native-kqueue:$nettyVersion:osx-x86_64")
    implementation("io.netty:netty-transport-native-epoll:$nettyVersion:linux-x86_64")
    implementation("com.fizzed:rocker-compiler:$rockerVersion")
}

application {
    mainClass.set("io.forestframework.benchmark.AppKt")
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
