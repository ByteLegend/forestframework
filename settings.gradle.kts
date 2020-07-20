//plugins {
//    id("com.gradle.enterprise") version("3.3.4")
//    id("com.gradle.enterprise.gradle-enterprise-conventions-plugin") version("0.3")
//}

include("core")
include("core-kotlin")
include("junit5-extension")
include("jdbc-client-extension")
include("redis-client-extension")
include("pg-client-extension")
include("jsr311-extension")
include("jsr303-extension")
include("cors-extension")
include("thymeleaf-extension")
include(":examples:todo-kotlin")
include(":examples:todo")
include("benchmark-kotlin")
include("test-fixtures")

rootProject.children.forEach {
    it.buildFileName = "${it.name}.gradle.kts"
}

project(":examples").children.forEach {
    it.buildFileName = "${it.name}.gradle.kts"
}