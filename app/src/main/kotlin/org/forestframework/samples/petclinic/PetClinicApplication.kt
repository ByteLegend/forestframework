package org.forestframework.samples.petclinic

import org.forestframework.Forest
import org.forestframework.annotation.ForestApplication
import org.forestframework.annotation.Get
import org.forestframework.annotation.ThymeleafTemplateRendering
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