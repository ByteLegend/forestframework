val vertxVersion = "4.2.6"
val guiceVersion = "5.0.1.1"
val guavaVersion = "31.0.1-jre"
val jacksonVersion = "2.13.0"
val kotlinVersion = "1.5.31"
val kotlinxVersion = "1.5.2"
val junitVersion = "5.8.1"
val junit4Version = "4.13.2"
val mockKVersion = "1.12.1"
val reflectasmVersion = "1.11.9"
val apiGuadianVersion = "1.1.0"
val log4jVersion = "2.13.3"
val slf4jVersion = "1.7.30"
val commonsLangVersion = "3.12.0"
val junitExtensionsVersion = "2.4.0"
val commonsIOVersion = "2.11.0"
val annotationMagicVersion = "0.2.5"
val jsr311ApiVersion = "1.1.1"
val gebVersion = "3.4.1"
val seleniumVersion = "3.141.59"
val groovyVersion = "2.5.12"
val findBugsAnnotationVersion = "3.0.1"
val httpclient = "4.5.13"
val mockitoVersion = "3.12.4"
val testContainersVersion = "1.16.2"

val dependencies = listOf(
    "io.vertx:vertx-core:$vertxVersion",
    "io.vertx:vertx-web:$vertxVersion",
    "io.vertx:vertx-zookeeper:$vertxVersion",
    "io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion",
    "io.vertx:vertx-lang-kotlin:$vertxVersion",
    "io.vertx:vertx-unit:$vertxVersion",
    "io.vertx:vertx-codegen:$vertxVersion",
    "io.vertx:vertx-redis-client:$vertxVersion",
    "io.vertx:vertx-jdbc-client:$vertxVersion",
    "io.vertx:vertx-pg-client:$vertxVersion",
    "io.vertx:vertx-web-client:$vertxVersion",

    "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion",
    "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxVersion",
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion",
    "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion",

    "javax.inject:javax.inject:1",
    "io.forestframework:guice:$guiceVersion",
    "javax.ws.rs:jsr311-api:1.1.1",

    "com.github.blindpirate:annotation-magic:$annotationMagicVersion",

    "org.slf4j:slf4j-api:$slf4jVersion",

    "com.fasterxml.jackson.core:jackson-core:$jacksonVersion",
    "com.fasterxml.jackson.core:jackson-databind:$jacksonVersion",
    "com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion",
    "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion",

    "com.esotericsoftware:reflectasm:$reflectasmVersion",
    "org.apache.commons:commons-lang3:${commonsLangVersion}",
    "commons-io:commons-io:$commonsIOVersion",
    "com.google.guava:guava:$guavaVersion",

    "org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion",
    "org.apache.logging.log4j:log4j-core:$log4jVersion",

    "org.apiguardian:apiguardian-api:$apiGuadianVersion",

    "io.mockk:mockk:$mockKVersion",

    "junit:junit:$junit4Version",
    "org.junit.jupiter:junit-jupiter-api:$junitVersion",
    "org.junit.jupiter:junit-jupiter-engine:$junitVersion",
    "org.junit.vintage:junit-vintage-engine:$junitVersion",
    "org.junit.jupiter:junit-jupiter-params:$junitVersion",
    "io.github.glytching:junit-extensions:$junitExtensionsVersion",

    "org.gebish:geb-core:$gebVersion",
    "org.testcontainers:testcontainers:$testContainersVersion",
    "org.testcontainers:selenium:$testContainersVersion",
    "org.testcontainers:junit-jupiter:$testContainersVersion",
    "org.seleniumhq.selenium:selenium-firefox-driver:${seleniumVersion}",
    "org.seleniumhq.selenium:selenium-api:${seleniumVersion}",
    "org.seleniumhq.selenium:selenium-chrome-driver:${seleniumVersion}",
    "org.seleniumhq.selenium:selenium-remote-driver:${seleniumVersion}",
    "org.seleniumhq.selenium:selenium-support:${seleniumVersion}",
    "org.codehaus.groovy:groovy-all:$groovyVersion",
    "com.google.code.findbugs:annotations:$findBugsAnnotationVersion",

    "org.apache.httpcomponents:httpclient:$httpclient",
    "org.mockito:mockito-core:$mockitoVersion",
    "org.mockito:mockito-junit-jupiter:$mockitoVersion"
)

val libs = mutableMapOf<String,String>()

dependencies.forEach {
    val artifactId = it.split(":")[1]
    if (libs.contains(artifactId)) {
        // artifactId conflict, remove
        val gav = libs.remove(artifactId)!!
        libs[it.substringBeforeLast(":")] = it
        libs[gav.substringBeforeLast(":")] = gav
    } else {
        libs[artifactId] = it
    }

    libs[it.substringBeforeLast(":")] = it
}

rootProject.extensions.configure<org.gradle.api.plugins.ExtraPropertiesExtension>("ext") {
    val function: (String) -> String = libs::getValue
    set("libs", function)
}
