include("core")
include("core-kotlin")
include("junit5-extension")
include("test-fixtures")

include(":extensions:jdbc-client-extension")
include(":extensions:redis-client-extension")
include(":extensions:pg-client-extension")
include(":extensions:jsr311-extension")
include(":extensions:jsr303-extension")
include(":extensions:cors-extension")
include(":extensions:thymeleaf-extension")

include(":examples:realtime-auctions")
include(":examples:todo")
include(":examples:petclinic")
include(":examples:benchmark-kotlin")
include(":examples:initializr")

rootProject.children.forEach {
    it.buildFileName = "${it.name}.gradle.kts"
}

listOf(":extensions", ":examples").forEach { p ->
    project(p).children.forEach {
        it.buildFileName = "${it.name}.gradle.kts"
    }
}