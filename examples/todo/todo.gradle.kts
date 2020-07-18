import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("application")
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
    implementation(project(":jdbc-client-extension"))
    implementation(project(":redis-client-extension"))
    implementation(project(":cors-extension"))
    implementation(project(":core-kotlin"))
    implementation("com.h2database:h2:$h2Version")
    testImplementation(project(":junit5-extension"))
    testImplementation("io.vertx:vertx-web-client:4.0.0-milestone5")
    testImplementation("se.svt.oss.junit5:junit5-redis-extension:$junitRedisExtension")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

application {
    mainClassName = "io.forestframework.ToDoApplicationKt"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}
