val libs: (String) -> String by rootProject.ext

dependencies {
    val embeddedRedisVersion = "0.7.3"

    api(project(":junit5-extension"))
    api(project(":core-kotlin"))
    api(libs("vertx-web-client"))
    api("it.ozimov:embedded-redis:$embeddedRedisVersion")
}