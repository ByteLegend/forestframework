package org.forestframework

import io.mockk.every
import io.mockk.mockk
import io.vertx.ext.web.RoutingContext
import org.forestframework.annotation.PathParam
import org.forestframework.annotation.Route
import org.forestframework.http.DefaultRouting
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


class PathParamResolverTest {
    @ParameterizedTest(name = "works with {0} and param {2}")
    @CsvSource(value = [
        "methodWithWildcard1, /a,                  0,    a",
        "methodWithWildcard2, /js/a/b/c.js,        0,    a/b/c.js",
        "methodWithPathParam, /user/123/pet/456,   0,    123",
        "methodWithPathParam, /user/123/pet/456,   1,    456",
        "methodWithRegex,     /js/a/b.js,          0,    a/b.js"
    ])
    @Suppress("UNUSED_PARAMETER")
    fun test(methodName: String, uri: String, paramIndex: Int, expectedValue: String) {
        val pathParamResolver = PathParamResolver()
        val routingContext = mockk<RoutingContext>()
        val method = PathParamResolverTestData::class.java.methods.find { it.name == methodName }
        val routing = DefaultRouting(method?.getAnnotation(Route::class.java), PathParamResolverTestData::class.java, method, null)
        if (!methodName.contains("Wildcard")) {
            every { routingContext.request().getParam(any()) } returns expectedValue
        } else {
            every { routingContext.request().absoluteURI() } returns uri
        }
        Assertions.assertEquals(expectedValue, pathParamResolver.resolveArgument(routing, routingContext, paramIndex))
    }
}

class PathParamResolverTestData {
    @Route("/*")
    fun methodWithWildcard1(@PathParam("*") wildcard: String) {
    }

    @Route("/js/*")
    fun methodWithWildcard2(@PathParam("*") wildcard: String) {
    }

    @Route("/user/:userId/pet/:petId")
    fun methodWithPathParam(@PathParam("userId") userId: String, @PathParam("petId") petId: String) {
    }

    @Route("""\/js\/(?<file>.+)""")
    fun methodWithRegex(@PathParam("file") file: String) {
    }
}