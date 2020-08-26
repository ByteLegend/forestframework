rootProject.extensions.configure<ExtraPropertiesExtension>("ext") {
    val function: Project.() -> Unit = {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")

        val thisProject = this
        val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer

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
    set("configurePublication", function)
}