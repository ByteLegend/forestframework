include("app")
include("forest")
include("forest-kotlin")
include("forest-jsr311-extension")
include("forest-jsr303-extension")
include("forest-thymeleaf-extension")
include("vertx-completable-future")

rootProject.children.forEach { project ->
    project.buildFileName = "${project.name}.gradle.kts"
}