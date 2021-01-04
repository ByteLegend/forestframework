rootProject.extensions.configure<ExtraPropertiesExtension>("ext") {
    val function: Project.() -> Unit = {
        apply(plugin = "groovy")
        apply(plugin = "codenarc")

        val sourceSets: SourceSetContainer by extensions

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
            systemProperty("recording.dir", project.buildDir)
        }
        tasks.named("check").configure { dependsOn(tasks.withType<Test>()) }
    }
    set("configureGroovy", function)
}
