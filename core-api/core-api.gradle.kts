apply(from = "../gradle/publication.gradle.kts")

val libs: (String) -> String by rootProject.ext
val configurePublication: Project.() -> Unit by rootProject.ext

dependencies {
}
