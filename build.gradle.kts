import com.github.spotbugs.snom.SpotBugsTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.spotbugs") version ("4.5.0") apply (false)
}

apply(from = "gradle/dependencies.gradle.kts")

val libs: (String) -> String by rootProject.ext

rootProject.group = "io.forestframework"
rootProject.version = "0.1-SNAPSHOT"

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven { url = URI("https://oss.sonatype.org/content/repositories/snapshots") }
    }

    configureJava()

    if (file("src/main/kotlin").isDirectory || file("src/test/kotlin").isDirectory) {
        configureKotlin()
    }

    if (file("src/browserTest/groovy").isDirectory) {
        configureGroovy()
    }
}

project("core").configurePublication()
project("core-kotlin").configurePublication()

fun Project.configureGroovy() {
    apply(plugin = "groovy")
    apply(plugin = "codenarc")
    sourceSets.create("browserTest") {
        withConvention(GroovySourceSet::class) {
            groovy.srcDir("src/core/groovy")
        }
        resources.srcDir("src/browserTest/resources")
    }
    dependencies {
        "browserTestImplementation"(sourceSets["main"].output)
        "browserTestImplementation"(sourceSets["test"].output)
        "browserTestImplementation"(configurations["testImplementation"])
    }

    tasks.register("browserTest", Test::class) {
        testClassesDirs = sourceSets["browserTest"].output.classesDirs
        classpath = sourceSets["browserTest"].runtimeClasspath
        useJUnitPlatform()
        mustRunAfter("test")
    }
    tasks.check.configure { dependsOn(tasks.withType<Test>()) }
}

fun Project.configureKotlin() {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    configureKtlint()

    tasks.withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "1.8"
    }

    dependencies {
        testImplementation(libs("kotlinx-coroutines-jdk8"))
        testImplementation(libs("kotlinx-coroutines-core"))
        testImplementation(libs("kotlin-stdlib-jdk8"))
    }
}

fun Project.configureJava() {
    apply(plugin = "jacoco")
    apply(plugin = "java-library")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.spotbugs")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.test {
        useJUnitPlatform()
    }

    tasks.named("check").configure {
        dependsOn("spotbugsMain")
    }

    tasks.withType<SpotBugsTask>().configureEach {
        isEnabled = name == "spotbugsMain" && !project.file("src/main/kotlin").isDirectory
        reports.maybeCreate("xml").isEnabled = false
        reports.maybeCreate("html").isEnabled = true
    }
}

fun Project.configureKtlint() {
    configurations.create("ktlint")
    dependencies {
        val ktlintVersion = "0.36.0"
        "ktlint"("com.pinterest:ktlint:$ktlintVersion")
    }

    val ktlintTask = tasks.register<JavaExec>("ktlint") {
        group = "verification"
        description = "Check Kotlin code style"
        classpath = configurations["ktlint"]
        main = "com.pinterest.ktlint.Main"
        args("src/main/**/*.kt", "src/test/**/*.kt")
    }

    tasks.named("check").configure { dependsOn(ktlintTask) }

    tasks.register<JavaExec>("ktlintFormat") {
        group = "formatting"
        description = "Fix Kotlin code style deviations"
        classpath = configurations["ktlint"]
        main = "com.pinterest.ktlint.Main"
        args("-F", "src/main/**/*.kt", "src/test/**/*.kt")
    }
}


extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}


fun Project.configurePublication() {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    val thisProject = this

    tasks.register<Jar>("sourcesJar") {
        from(sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }

    tasks.register<Jar>("javadocJar") {
        from(tasks.named("javadoc"))
        archiveClassifier.set("javadoc")
    }

    extensions.configure<PublishingExtension>("publishing") {
        publications {
            create<MavenPublication>("maven") {
                groupId = rootProject.group.toString()
                artifactId = thisProject.name
                version = rootProject.version.toString()

                from(components.getByName("java"))
                artifact(tasks.getByName("sourcesJar"))
                artifact(tasks.getByName("javadocJar"))

                pom {
                    name.set(thisProject.name)
                    description.set("")
                    url.set("")
                    licenses {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }

                    developers {
                        developer {
                            id.set("blindpirate")
                            name.set("Bo Zhang")
                            email.set("zhangbodut@gmail.com")
                        }
                    }
                    scm {
                        connection.set("")
                        developerConnection.set("")
                        url.set("")
                    }
                }
            }
        }
        repositories {
            maven {
                setUrl(if (rootProject.version.toString().endsWith("SNAPSHOT")) "https://oss.sonatype.org/content/repositories/snapshots" else
                    "https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = findProperty("NEXUS_USERNAME")?.toString() ?: ""
                    password = findProperty("NEXUS_PASSWORD")?.toString() ?: ""
                }
            }
        }
    }
    extensions.configure<SigningExtension>("signing") {
        sign(extensions.getByType(PublishingExtension::class.java).publications.getByName("maven"))
    }
}