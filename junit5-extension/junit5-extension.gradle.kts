val libs: (String) -> String by rootProject.ext

dependencies {
    api(project(":core"))
    api(libs("junit-jupiter-api"))

    implementation(libs("junit-jupiter-engine"))
    implementation(libs("junit-jupiter-engine"))
    implementation(libs("commons-lang3"))

    testImplementation(project(":test-fixtures"))
}

val configurePublication: Project.() -> Unit by rootProject.ext
configurePublication()
