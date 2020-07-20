val libs: (String) -> String by rootProject.ext

dependencies {
    api(libs("kotlinx-coroutines-jdk8"))
    api(libs("kotlinx-coroutines-core"))
    api(libs("kotlin-stdlib-jdk8"))
    api(libs("vertx-lang-kotlin"))
    api(libs("vertx-lang-kotlin-coroutines"))
    api(project(":core"))
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "junit-vintage")
    }
}
