val libs: (String) -> String by rootProject.ext

dependencies {
    api(libs("vertx-redis-client"))
    implementation(project(":core"))
}
