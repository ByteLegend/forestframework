package io.forestframework.ext.core

import io.forestframework.core.Component
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.Router
import io.forestframework.core.http.routing.Get

class RouterWithoutAnno {
    @Get(path = "/path")
    fun path() {
    }

    @Get(regex = "/regexPath")
    fun regexPath() {
    }
}

@Router
class RouterWithRouterAnno

@Router("/routerPath")
class RouterWithRouterAnnoPath

@Component
class RouterWithComponentAnno

@ForestApplication
class RouterWithForestApplicationAnno

class AutoRoutingScanExtensionTest
