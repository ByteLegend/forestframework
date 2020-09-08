//val vertxVersion = "4.0.0-milestone5"
val vertxVersion = "4.0.0.Beta1"
val guiceVersion = "4.2.3-SNAPSHOT"
val guavaVersion = "28.2-jre"
val jacksonVersion = "2.10.3"
val kotlinVersion = "1.3.72"
val kotlinxVersion = "1.3.8"
val junitVersion = "5.6.2"
val junit4Version = "4.13"
val mockKVersion = "1.9.3"
val reflectasmVersion = "1.11.9"
val apiGuadianVersion = "1.1.0"
val log4jVersion = "2.13.3"
val slf4jVersion = "1.7.30"
val commonsLangVersion = "3.10"
val junitExtensionsVersion = "2.4.0"
val commonsIOVersion = "2.7"
val annotationMagicVersion = "0.2.5"
val jsr311ApiVersion = "1.1.1"
val gebVersion = "3.4"
val seleniumVersion = "3.141.59"
val groovyVersion = "2.5.12"
val findBugsAnnotationVersion = "3.0.1"
val httpclient = "4.5.12"
val mockitoVersion = "3.5.10"

val libs = listOf(
    "io.vertx:vertx-core:$vertxVersion",
    "io.vertx:vertx-web:$vertxVersion",
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
    "org.seleniumhq.selenium:selenium-firefox-driver:${seleniumVersion}",
    "org.seleniumhq.selenium:selenium-chrome-driver:${seleniumVersion}",
    "org.seleniumhq.selenium:selenium-support:${seleniumVersion}",
    "org.codehaus.groovy:groovy-all:$groovyVersion",
    "com.google.code.findbugs:annotations:$findBugsAnnotationVersion",

    "org.apache.httpcomponents:httpclient:$httpclient",
    "org.mockito:mockito-core:$mockitoVersion",
    "org.mockito:mockito-junit-jupiter:$mockitoVersion"
).map { it.split(":")[1] to it }.toMap()

rootProject.extensions.configure<org.gradle.api.plugins.ExtraPropertiesExtension>("ext") {
    val function: (String) -> String = libs::getValue
    set("libs", function)
}