val libs: (String) -> String by rootProject.ext

dependencies {
    api(project(":junit5-extension"))
    api(project(":core-kotlin"))
    api(libs("vertx-web-client"))
}