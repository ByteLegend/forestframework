include("petclinic-example")
include("core")
include("kotlin")
include("jdbc-client-extension")
include("redis-client-extension")
include("jsr311-extension")
include("jsr303-extension")
include("thymeleaf-extension")
include("vertx-completable-future")
include("realtime-auctions-example")
include("todo-backend-example")

rootProject.children.forEach { project ->
    project.buildFileName = "${project.name}.gradle.kts"
}