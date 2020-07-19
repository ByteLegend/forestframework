plugins {
    id("java-library")
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
//    val vertxVersion = "4.0.0-milestone5"
    val junit5Version = "5.6.2"
    val commonsLangVersion = "3.10"
    api(project(":core"))
    api("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    implementation("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
    implementation("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
    implementation("org.apache.commons:commons-lang3:${commonsLangVersion}")
}
