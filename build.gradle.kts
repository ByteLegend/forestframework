import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
}
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//
//plugins {
//    id("org.jetbrains.kotlin.jvm") version "1.3.72"
//}
//
//allprojects {
//    repositories {
//        jcenter()
//        mavenCentral()
//    }
//
//    when {
//        projectDir.resolve("src/main/kotlin").isDirectory -> configureKotlinProject()
//        projectDir.resolve("src/main/java").isDirectory -> configureJavaProject()
//    }
//
//    if (projectDir.resolve("src/main/test").isDirectory) {
//        tasks.named("test", Test::class.java) {
//            useJUnitPlatform()
//        }
//    }
//}
//
//project("teamcity-workaround") {
//    dependencies {
//        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
//        api("commons-codec:commons-codec:1.10")
//        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//        implementation("com.squareup.retrofit:retrofit:1.9.0")
//        implementation("com.jakewharton.retrofit:retrofit1-okhttp3-client:1.1.0")
//        implementation("org.slf4j:slf4j-api:1.7.12")
//    }
//}
//
//project("app") {
//    configureKtlint()
//
//    apply(plugin = "application")
//    apply(from = "../gradle/json2Java.gradle.kts")
//
//    tasks.named("run", JavaExec::class.java) {
//        main = "org.gradle.bot.AppKt"
//    }
//
//    tasks.named("compileJava") {
//        dependsOn("json2Java")
//    }
//
//    configure<JavaApplication> {
//        mainClassName = "org.gradle.bot.AppKt"
//
////        applicationDistribution.from(sourceSets["teamCityWorkaround"].output) {
////            into("lib/teamCityWorkaround")
////        }
//    }
//
//    dependencies {
//        val vertxVersion = "4.0.0-milestone4"
//        val jacksonVersion = "2.10.3"
//        val logbackVersion = "1.2.3"
//        val junit5Version = "5.6.1"
//        val guavaVersion = "28.2-jre"
//        val mockitoJUnitVersion = "3.3.3"
//        val mockKVersion = "1.9.3"
//        val guiceVersion = "4.2.3"
//
//        implementation(project(":teamcity-workaround"))
//
//        api("io.vertx:vertx-core:$vertxVersion")
//        api("com.google.inject:guice:$guiceVersion")
//
//        implementation("io.vertx:vertx-web-client:$vertxVersion")
//        implementation("io.vertx:vertx-core:$vertxVersion")
//        implementation("io.vertx:vertx-web:$vertxVersion")
//        implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
//        implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
//
//        implementation("javax.inject:javax.inject:1")
//
//        implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
//        implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
//        implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
//        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
//        // Use logback logging
//        implementation("ch.qos.logback:logback-classic:$logbackVersion")
//        implementation("ch.qos.logback:logback-core:$logbackVersion")
//
//        implementation("com.google.guava:guava:$guavaVersion")
//
//        //junit 5
//        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
//        testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
//        testImplementation("org.junit.jupiter:junit-jupiter-params:$junit5Version")
//        testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJUnitVersion")
//        testImplementation("io.mockk:mockk:$mockKVersion")
//    }
//}
//
//fun Project.configureKotlinProject() {
//    apply(plugin = "org.jetbrains.kotlin.jvm")
//
//    tasks.withType<KotlinCompile>() {
//        kotlinOptions.jvmTarget = "1.8"
//    }
//}
//
//fun Project.configureJavaProject() {
//    apply(plugin = "java-library")
//}
//
//fun Project.configureKtlint() {
//    configurations.create("ktlint")
//    dependencies {
//        val ktlintVersion = "0.36.0"
//        "ktlint"("com.pinterest:ktlint:$ktlintVersion")
//    }
//
//    val ktlintTask = tasks.register<JavaExec>("ktlint") {
//        group = "verification"
//        description = "Check Kotlin code style"
//        classpath = configurations["ktlint"]
//        main = "com.pinterest.ktlint.Main"
//        args("src/main/**/*.kt", "src/test/**/*.kt")
//    }
//
//    tasks.named("check").configure { dependsOn(ktlintTask) }
//
//    tasks.register<JavaExec>("ktlintFormat") {
//        group = "formatting"
//        description = "Fix Kotlin code style deviations"
//        classpath = configurations["ktlint"]
//        main = "com.pinterest.ktlint.Main"
//        args("-F", "src/main/**/*.kt", "src/test/**/*.kt")
//    }
//}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}