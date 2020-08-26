import com.github.spotbugs.snom.SpotBugsTask

// just for script compilation
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.github.spotbugs:com.github.spotbugs.gradle.plugin:4.5.0")
    }
}

rootProject.extensions.configure<ExtraPropertiesExtension>("ext") {
    val function: Project.() -> Unit = {
        apply(plugin = "jacoco")
        apply(plugin = "java-library")
        apply(plugin = "checkstyle")
        apply(plugin = "com.github.spotbugs")

        extensions.configure<JavaPluginExtension>("java") {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        tasks.named<Test>("test") {
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
    set("configureJava", function)
}