package org.forestframework.samples.petclinic.system

import org.forestframework.annotation.Get
import org.forestframework.annotation.Router
import org.forestframework.annotation.StaticResource

@Router
class WelcomeRouter {
    @Get("/")
    @StaticResource
    fun welcome() = "welcome"
}