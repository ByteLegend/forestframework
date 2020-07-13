include("core")
include("junit5-extension")
include("jdbc-client-extension")
include("redis-client-extension")
include("pg-client-extension")
include("jsr311-extension")
include("jsr303-extension")
include("cors-extension")
include("thymeleaf-extension")
include("petclinic-example")
include("realtime-auctions-example")
include(":examples:todo-kotlin")
include(":examples:todo")
include("benchmark-kotlin")

rootProject.children.forEach {
    it.buildFileName = "${it.name}.gradle.kts"
}

project(":examples").children.forEach {
    it.buildFileName = "${it.name}.gradle.kts"
}