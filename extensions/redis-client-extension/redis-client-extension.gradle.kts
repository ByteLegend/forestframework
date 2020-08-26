val libs: (String) -> String by rootProject.ext

dependencies {
    api(libs("vertx-redis-client"))
    implementation(project(":core"))
}

val configurePublication: Project.() -> Unit by rootProject.ext
configurePublication()