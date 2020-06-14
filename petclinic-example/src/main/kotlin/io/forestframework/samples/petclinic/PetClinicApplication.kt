package io.forestframework.samples.petclinic

import io.forestframework.core.Forest
import io.forestframework.annotation.ForestApplication
import io.forestframework.annotation.Get
import io.forestframework.annotation.ThymeleafTemplateRendering
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