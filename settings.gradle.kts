include("petclinic-example")
include("core")
include("jdbc-client-extension")
include("redis-client-extension")
include("pg-client-extension")
include("jsr311-extension")
include("jsr303-extension")
include("cors-extension")
include("thymeleaf-extension")
include("vertx-completable-future")
include("realtime-auctions-example")
include("todo-backend-example")
include("annotation-magic")
include("benchmark-kotlin")

rootProject.children.forEach { project ->
    project.buildFileName = "${project.name}.gradle.kts"
}