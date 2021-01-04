val libs: (String) -> String by rootProject.ext

dependencies {
    val embeddedRedisVersion = "0.7.3"

    api(project(":junit5-extension"))
    api(project(":core-kotlin"))
    api(libs("vertx-web-client"))
    api("it.ozimov:embedded-redis:$embeddedRedisVersion")
    api(libs("junit-jupiter-params"))
    api(libs("junit-jupiter-api"))
    api(libs("geb-core"))
    api(libs("org.testcontainers:testcontainers"))
    implementation(libs("org.testcontainers:selenium"))
    implementation(libs("org.testcontainers:junit-jupiter"))
    api(libs("selenium-api"))
    implementation(libs("selenium-chrome-driver"))

    implementation(libs("selenium-firefox-driver"))
    implementation(libs("selenium-chrome-driver"))
    implementation(libs("selenium-support"))
    implementation(libs("httpclient"))
    runtimeOnly(libs("junit-jupiter-engine"))
}
