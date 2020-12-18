package io.forestframework.core.http

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.routing.Get
import io.forestframework.core.http.routing.OnError
import io.forestframework.core.http.routing.PreHandler
import io.forestframework.core.http.websocket.OnWSError
import io.forestframework.ext.core.IncludeComponents
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Collections
import javax.inject.Inject
import kotlin.collections.ArrayList

@ForestApplication
class CustomErrorHandlerApp {
    fun errorInPreHandler() {
    }

    fun errorInHandler() {
    }

    fun errorInPostHandler() {
    }

    fun errorInErrorHandler() {
    }

    @OnWSError
    fun errorHandler() {
    }
}

const val custom500HandlerMessage = "Custom500Handler is handling "

abstract class AbstractTracingRouter {
    val traces: MutableList<String> = Collections.synchronizedList(ArrayList())

    fun runUnderTrace(traceId: String, fn: AbstractTracingRouter.() -> Unit) {
        traces.add(traceId)
        fn()
    }

    suspend fun runUnderTraceSuspend(traceId: String, fn: suspend AbstractTracingRouter.() -> Unit) {
        traces.add(traceId)
        fn()
    }
}

@Router("/custom500")
class Custom500HandlerRouter : AbstractTracingRouter() {

    @Get("/errorInHandler")
    suspend fun errorInHandler() = runUnderTraceSuspend("errorInHandler") {
        throw RuntimeException("errorInHandler")
    }

    @PreHandler(value = "/errorInPreHandler")
    suspend fun errorInPreHandler(): Unit = runUnderTraceSuspend("errorInPreHandler") {
        throw RuntimeException("errorInPreHandler")
    }

    @Get("/errorInPreHandler")
    suspend fun errorInPreHandler_Handler() = runUnderTraceSuspend("Not reachable") {
    }

    @OnError("/**")
    suspend fun on500(response: HttpResponse, e: Throwable) = runUnderTraceSuspend("custom500Handler") {
        response.writeLater(custom500HandlerMessage)
        response.writeLater(e.message!!)
    }
}

@Router("/errorInCustomErrorHandler")
class ErrorInCustomErrorHandler {
    @OnError("/**")
    fun errorHandler(): Unit = throw RuntimeException("errorInErrorHandler")
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = CustomErrorHandlerApp::class)
@DisableAutoScan
@IncludeComponents(classes = [Custom500HandlerRouter::class, ErrorInCustomErrorHandler::class])
class HandlerExecutionIntegrationTest : AbstractForestIntegrationTest() {
    @Inject
    lateinit var custom500HandlerRouter: Custom500HandlerRouter

    @BeforeEach
    fun cleanUpTraces() {
        custom500HandlerRouter.traces.clear()
    }

    @ParameterizedTest
    @ValueSource(strings = ["errorInHandler", "errorInPreHandler"])
    fun `exceptions in pre handlers and handlers are captured by custom 500 handler`(handler: String) = runBlockingUnit {
        get("/custom500/$handler").bodyAsString().apply {
            Assertions.assertEquals(listOf(handler, "custom500Handler"), custom500HandlerRouter.traces)
            assertThat(this, containsString(handler))
            assertThat(this, containsString(custom500HandlerMessage))
        }
    }

    @Test
    fun `exceptions in custom error handlers are handled by fallback error handler`() = runBlockingUnit {
        get("/errorInCustomErrorHandler/inexistent").bodyAsString().apply {
            assertThat(this, containsString(HttpStatusCode.NOT_FOUND.name))
        }
    }

    @Test
    fun `pre handlers continue when previous ones return void or true`() {
    }

    @Test
    fun `pre handlers return false then the following handlers are skipped`() {
    }

    @Test
    fun `throw exceptions when pre handlers not return Boolean`() {
    }

    fun `404 error is handled by custom 404 handler`() {
    }

    fun `405 406 415 is handled by custom handler`() {
    }

    fun `uncaught 404 error is handled by fallback error handler`() {
    }

    fun `HttpException can be thrown from pre handlers to prevent propagation`() {
    }

    fun `exceptions in post handlers are handled by fallback error handler but not custom handler`() {
    }
}
