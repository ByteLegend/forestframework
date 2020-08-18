package io.forestframework.example.petclinic

import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.result.ThymeleafTemplateRendering
import io.forestframework.core.http.routing.Get
import javax.inject.Singleton

@ForestApplication
@Singleton
class PetClinicApplication {
    @Get("/")
    @ThymeleafTemplateRendering
    fun welcome() = "welcome"
}

fun main() {
    Forest.run(PetClinicApplication::class.java)
}
