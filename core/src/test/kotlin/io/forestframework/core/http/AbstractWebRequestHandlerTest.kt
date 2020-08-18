package io.forestframework.core.http

import com.google.inject.Injector
import io.forestframework.core.http.routing.Routing
import io.forestframework.core.http.websocket.AbstractWebContext
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.vertx.core.Vertx
import org.apache.commons.lang3.exception.ExceptionUtils
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AbstractWebRequestHandlerTest {
    @MockK
    lateinit var vertx: Vertx

    @MockK
    lateinit var injector: Injector

    @RelaxedMockK
    lateinit var context: AbstractWebContext

    lateinit var routing: Routing

    lateinit var abstractWebRequestHandler: AbstractWebRequestHandler

    @BeforeEach
    fun setUp() {
        abstractWebRequestHandler = object : AbstractWebRequestHandler(vertx, injector) {
        }
        routing = Routing { throw RuntimeException("unlucky!") }
    }

    @Test
    fun `invokeRoutings should not throw exceptions under any circumstances`() {
        val future = abstractWebRequestHandler.invokeRouting<Any>(routing, context)

        val exception = assertThrows(Exception::class.java) {
            future.get()
        }

        assertThat(ExceptionUtils.getStackTrace(exception), CoreMatchers.containsString("unlucky!"))
    }
}
