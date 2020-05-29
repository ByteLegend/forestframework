include("petclinic-example")
include("core")
include("kotlin")
include("jsr311-extension")
include("jsr303-extension")
include("thymeleaf-extension")
include("vertx-completable-future")
include("realtime-auctions-example")

rootProject.children.forEach { project ->
    project.buildFileName = "${project.name}.gradle.kts"
}