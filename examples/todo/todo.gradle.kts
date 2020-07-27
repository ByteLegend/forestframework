import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("application")
    id("groovy")
}

repositories {
    jcenter()
    mavenCentral()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    val junitVersion = "5.6.2"
    val junitRedisExtension = "2.0.0"
    val h2Version = "1.4.200"
    val groovyVersion = "2.5.12"
    val jedisVersion = "3.2.0"
    implementation(project(":jdbc-client-extension"))
    implementation(project(":redis-client-extension"))
    implementation(project(":cors-extension"))
    implementation(project(":core-kotlin"))
    implementation("redis.clients:jedis:$jedisVersion")
    implementation("com.h2database:h2:$h2Version")
    testImplementation(project(":junit5-extension"))
    testImplementation("io.vertx:vertx-web-client:4.0.0-milestone5")
    testImplementation("se.svt.oss.junit5:junit5-redis-extension:$junitRedisExtension")
    testImplementation("org.codehaus.groovy:groovy-all:$groovyVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.gebish:geb-core:3.4")
    testImplementation("org.seleniumhq.selenium:selenium-firefox-driver:3.141.59")
    testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:3.141.59")
    testImplementation("org.seleniumhq.selenium:selenium-support:3.141.59")
}

application {
    mainClassName = "io.forestframework.ToDoApplicationKt"
}

tasks.test {
    useJUnitPlatform()
}
