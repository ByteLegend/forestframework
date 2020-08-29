rootProject.extensions.configure<ExtraPropertiesExtension>("ext") {
    val function: Project.() -> Unit = {
//        apply(plugin = "jacoco")
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
    }
    set("configureJava", function)
}