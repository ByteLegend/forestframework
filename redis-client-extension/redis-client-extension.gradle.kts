plugins {
    id("java-library")
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    val vertxVersion = "4.0.0-milestone5"
    api("io.vertx:vertx-redis-client:$vertxVersion")
    implementation(project(":core"))
}
