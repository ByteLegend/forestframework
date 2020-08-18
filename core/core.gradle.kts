val libs: (String) -> String by rootProject.ext

dependencies {
    api(libs("vertx-core"))
    implementation(libs("vertx-web"))
    api(libs("javax.inject"))
    api(libs("guice"))
    api(libs("annotation-magic"))
    api(libs("slf4j-api"))
    api(libs("jackson-core"))
    api(libs("jackson-databind"))
    api(libs("jackson-annotations"))
    api(libs("jackson-dataformat-yaml"))

    compileOnly(libs("kotlinx-coroutines-jdk8"))
    compileOnly(libs("kotlinx-coroutines-core"))
    compileOnly(libs("kotlin-stdlib-jdk8"))
    compileOnly(libs("vertx-lang-kotlin-coroutines"))
    // com.google.code.findbugs:annotations
    compileOnly(libs("annotations"))

    implementation(libs("reflectasm"))
    implementation(libs("commons-lang3"))
    implementation(libs("commons-io"))
    implementation(libs("guava"))
    implementation(libs("log4j-slf4j-impl"))
    implementation(libs("log4j-core"))
    implementation(libs("apiguardian-api"))

    testImplementation(project(":test-fixtures"))

    testImplementation(libs("vertx-lang-kotlin-coroutines"))
    testImplementation(libs("vertx-unit"))
    testImplementation(libs("vertx-codegen"))  // Keep this or https://github.com/mockito/mockito/issues/366
    testImplementation(libs("vertx-redis-client"))

    testImplementation(libs("junit"))
    testImplementation(libs("junit-extensions"))

    testImplementation(libs("mockk"))

    testRuntimeOnly(libs("junit-vintage-engine"))
}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "junit-vintage")
    }
}
