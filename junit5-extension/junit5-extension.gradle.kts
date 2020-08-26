val libs: (String) -> String by rootProject.ext

dependencies {
    api(project(":core"))
    api(libs("junit-jupiter-api"))

    implementation(libs("junit-jupiter-engine"))
    implementation(libs("junit-jupiter-engine"))
    implementation(libs("commons-lang3"))
}

val configurePublication: Project.() -> Unit by rootProject.ext
configurePublication()
