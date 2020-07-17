@file:Suppress("UNUSED_PARAMETER")

package io.forestframework.core.http.routing

import com.github.blindpirate.annotationmagic.AnnotationMagic
import com.google.inject.Injector
import io.forestframework.core.http.DefaultRouting
import io.forestframework.core.http.param.JsonRequestBody
import io.forestframework.core.http.result.GetJson
import io.forestframework.core.http.result.JsonResponseBody
import io.forestframework.core.http.result.PlainText
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FastRequestHandlerTest {
    @MockK
    lateinit var vertx: Vertx

    @MockK
    lateinit var routings: DefaultRoutings

    @MockK
    lateinit var injector: Injector

    @MockK
    lateinit var request: HttpServerRequest

    @RelaxedMockK
    lateinit var response: HttpServerResponse

    @RelaxedMockK
    lateinit var next: RequestHandler

    lateinit var fastRequestHandler: FastRequestHandler

    @BeforeEach
    fun setUp() {
        every { routings.getRoutingPrefixes(RoutingType.PRE_HANDLER) } returns emptyList()
        every { routings.getRoutingPrefixes(RoutingType.POST_HANDLER) } returns emptyList()
        every { routings.getRouting(RoutingType.HANDLER) } returns
            TestRouter::class.java.methods
                .filter { AnnotationMagic.isAnnotationPresent(it, Route::class.java) }
                .map { DefaultRouting(AnnotationMagic.getOneAnnotationOnMethodOrNull(it, Route::class.java), it) }

        every { request.response() } returns response
        every { injector.getInstance(any() as Class<*>) } answers { firstArg<Class<*>>().getConstructor().newInstance() }

        fastRequestHandler = FastRequestHandler(injector, vertx, routings, "dev")
        fastRequestHandler.setNext(next)
    }

    @Test
    fun `handle fast routings`() {
        every { request.path() } returns "/fast1"
        every { request.method() } returns HttpMethod.GET

        fastRequestHandler.handle(request)

        verify {
            response.end("[]")
        }
    }

    @Test
    fun `can distinguish same path different method`() {
        every { request.path() } returns "/fast1"
        every { request.method() } returns HttpMethod.POST

        fastRequestHandler.handle(request)

        verify {
            response.end("hello")
        }
    }

    @Test
    fun `not handle slow routings`() {
        every { request.path() } returns "/slow1"
        every { request.method() } returns HttpMethod.POST

        fastRequestHandler.handle(request)

        verify { next.handle(request) }
    }
}

// Don't test suspend function here, you'll waste your time.
class TestRouter {
    @GetJson("/fast1")
    fun fast1() = emptyList<Any>()

    @Post("/fast1")
    @PlainText
    fun fast1Post() = "hello"

    @Post("/slow1")
    @JsonResponseBody(pretty = true)
    fun slow1(@JsonRequestBody param: String, r: RoutingContext) = mapOf("name" to param)
}