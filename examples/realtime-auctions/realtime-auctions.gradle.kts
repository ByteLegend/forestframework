val libs: (String) -> String by rootProject.ext

dependencies {
    implementation(project(":core-kotlin"))

    testImplementation(project(":test-fixtures"))
    testImplementation(libs("groovy-all"))
}
