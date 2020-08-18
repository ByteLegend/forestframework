package io.forestframework.core.http

import io.forestframework.core.http.routing.Get

@Router("/preHandlersChain")
class PreHandlerIntegrationTest : AbstractTracingRouter() {
    @Get("/get")
    fun preHandle1() = runUnderTrace("preHandler1") {}
}
