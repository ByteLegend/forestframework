plugins {
    kotlin("jvm") version "1.4.10" apply false
    id("com.github.spotbugs") version ("4.5.0") apply false
}

apply(from = "gradle/dependencies.gradle.kts")
apply(from = "gradle/java-project.gradle.kts")
apply(from = "gradle/groovy-project.gradle.kts")
apply(from = "gradle/kotlin-project.gradle.kts")
apply(from = "gradle/build-scan.gradle.kts")

val libs: (String) -> String by rootProject.ext
val configureJava: Project.() -> Unit by rootProject.ext
val configureGroovy: Project.() -> Unit by rootProject.ext
val configureKotlin: Project.() -> Unit by rootProject.ext

rootProject.group = "io.forestframework"
rootProject.version = "0.1.4-SNAPSHOT"

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    }

    configureJava()
    tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().all {
        isEnabled = name.endsWith("spotbugsMain") && !project.file("src/main/kotlin").isDirectory
        reports.maybeCreate("xml").isEnabled = false
        reports.maybeCreate("html").isEnabled = true
    }

    if (file("src/main/kotlin").isDirectory || file("src/test/kotlin").isDirectory) {
        configureKotlin()
        // Must be here, otherwise it won't be configured because of different classloaders
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    if (file("src/browserTest/groovy").isDirectory) {
        configureGroovy()
    }
}
