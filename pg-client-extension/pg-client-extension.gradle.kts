plugins {
    id("java-library")
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    val vertxVersion = "4.0.0-milestone5"
    val reactivePgClientVersion = "0.11.4"
    api("io.vertx:vertx-pg-client:$vertxVersion")
    api("io.reactiverse:reactive-pg-client:$reactivePgClientVersion")
    implementation(project(":core"))
}
