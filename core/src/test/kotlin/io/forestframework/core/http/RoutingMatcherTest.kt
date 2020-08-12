package io.forestframework.core.http;

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.net.MediaType
import io.forestframework.core.http.HttpMethod.ALL
import io.forestframework.core.http.HttpMethod.DELETE
import io.forestframework.core.http.HttpMethod.GET
import io.forestframework.core.http.HttpMethod.PATCH
import io.forestframework.core.http.HttpMethod.POST
import io.forestframework.core.http.routing.Routing
import io.forestframework.core.http.routing.RoutingManager
import io.forestframework.core.http.routing.RoutingType
import io.forestframework.core.http.routing.RoutingType.HANDLER
import io.forestframework.core.http.routing.RoutingType.POST_HANDLER
import io.forestframework.core.http.routing.RoutingType.PRE_HANDLER
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.vertx.core.http.HttpServerRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class RoutingMatcherTest {
    @MockK
    lateinit var routingManager: RoutingManager

    @MockK
    lateinit var request: HttpServerRequest

    val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        every { routingManager.getRouting(any()) } returns emptyList()
        every { request.method() } returns io.vertx.core.http.HttpMethod.GET
        every { request.getHeader(any<CharSequence>()) } returns "*/*"
    }

    private fun mockRouting(path: String,
                            order: Int = 0,
                            method: HttpMethod = ALL,
                            methods: List<HttpMethod>? = null,
                            type: RoutingType = HANDLER,
                            consumes: String = "*/*",
                            produces: String = "*/*"): Routing {
        val ret = mockk<Routing>()
        every { ret.path } returns path
        every { ret.regexPath } returns ""
        every { ret.methods } returns (methods ?: listOf(method))
        every { ret.order } returns order
        every { ret.type } returns type
        every { ret.consumes } returns listOf(consumes)
        every { ret.produces } returns listOf(produces)
        return ret
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(value = [
        "/**,            /index.html,              true, index.html",
        "/**,            /static/css/1.css,        true, static/css/1.css",
        "/**,            /,                        true, '' ",
        "/**,            /user/123/order/456,      true, user/123/order/456",
        "/**/order/**,   /user/123/order/456,      true, 456",
        "/**/order/**,   /user/123/order/456/a/b,  true, 456/a/b",
        "/**/order/**,   /user/order/1,            true,  1",
        "/**/order/**,   /user/order/1/2/3,        true, 1/2/3",
        "/**/order/**,   /order/1,                 true, 1",
        "/**/order/**,   /order/1/2/3,             true, 1/2/3",
        "/order/:a/**,   /order/123/a/b/c,         true, a/b/c",
        "/order/:a/**,   /order/123,               false, '' ",
        "/a/**/b,        /a/b,                     true, '' ",
        "/a/**/b,        /a/c,                     false, '' ",
        "/a/**/b,        /a/1/b,                   true, 1",
        "/a/**/b,        /a/1/2/b,                 true, 1/2",
        "/a/**/b,        /a/1/2/b/c,               false, '' "
    ])
    fun `can match double star wildcard`(route: String, requestPath: String, matches: Boolean, expectedPathVariable: String) {
        every { routingManager.getRouting(HANDLER) } returns listOf(mockRouting(route))
        every { request.path() } returns requestPath

        val routingMatcher = RoutingMatcher(routingManager)

        if (matches) {
            assertEquals(route, routingMatcher.match(request).getMatchedRoutings(HANDLER)[0].path)
            assertEquals(expectedPathVariable, routingMatcher.match(request).getMatchedResults(HANDLER).first()?.pathParams?.get("**"))
        } else {
            assertTrue(routingMatcher.match(request).getMatchedRoutings(HANDLER).isEmpty())
        }
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(value = [
        "/*,          /index.html,          true,  index.html",
        "/*,          /,                    true,  '' ",
        "/*,          /user/123/order/456,  false, '' ",
        "/*abc,       /abc,                 true,  '' ",
        "/a*b*c,      /abc,                 true,  '' ",
        "/*a*b*c,     /abc,                 true,  '' ",
        "/a*/*b/*c*,  /a/b/c,               true,  '' "
    ])
    fun `can match single star wildcard`(route: String, requestPath: String, matches: Boolean, expectedPathVariable: String) {
        every { routingManager.getRouting(HANDLER) } returns listOf(mockRouting(route))
        every { request.path() } returns requestPath

        val routingMatcher = RoutingMatcher(routingManager)

        if (matches) {
            assertEquals(route, routingMatcher.match(request).getMatchedRoutings(HANDLER)[0].path)
            assertEquals(expectedPathVariable, routingMatcher.match(request).getMatchedResults(HANDLER)?.first()?.pathParams?.get("*"))
        } else {
            assertTrue(routingMatcher.match(request).getMatchedRoutings(HANDLER).isEmpty())
        }
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(value = [
        """/user/:userId/order/:orderId; /user/123/order/456; true; {"userId": "123", "orderId": "456"}""",
        """/:a/:b/:c/:d;                /user/123/order/456; true; {"a": "user", "b": "123", "c": "order", "d": "456"}""",
        """/:a/:b/:c;                 /user/123/order/456; false; '' """,
        """/:user/:userId/order;      /user/123/order/456; false; '' """
    ], delimiter = ';')
    fun `can match and extract path variables`(route: String, requestPath: String, matches: Boolean, expectedPathVariableJson: String) {
        every { routingManager.getRouting(HANDLER) } returns listOf(mockRouting(route))
        every { request.path() } returns requestPath

        val routingMatcher = RoutingMatcher(routingManager)

        if (matches) {
            assertEquals(route, routingMatcher.match(request).getMatchedRoutings(HANDLER)[0].path)
            assertEquals(objectMapper.readValue(expectedPathVariableJson, Map::class.java),
                routingMatcher.match(request).getMatchedResults(HANDLER).first()?.pathParams)
        } else {
            assertTrue(routingMatcher.match(request).getMatchedRoutings(HANDLER).isEmpty())
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "/",
        "/index.html",
        "/static/js/app.js",
        "/a/b/c/d",
        "/org/repo/branches"
    ])
    fun `match results are strictly ordered`(requestPath: String) {
        every { routingManager.getRouting(HANDLER) } returns listOf(
            mockRouting("/**", method = GET, order = -1),
            mockRouting("/**", method = POST, order = 100, consumes = "application/json"),
            mockRouting("/**", method = POST, order = 101, consumes = "application/xml"),
            mockRouting("/**", method = POST, order = 99, consumes = "text/plain"),
            mockRouting("/**", method = PATCH, order = 2),
            mockRouting("/**", method = DELETE, order = 1000)
        )

        every { request.path() } returns requestPath

        assertEquals(listOf(-1, 2, 99, 100, 101, 1000),
            RoutingMatcher(routingManager).match(request).getMatchedResults(HANDLER).map { it.routing.order })
    }

    @Test
    fun `can match multi level results`() {
        every { routingManager.getRouting(HANDLER) } returns listOf(
            mockRouting("/**", order = 4),
            mockRouting("/**/d", order = 1),
            mockRouting("/**/c/d", order = 0),
            mockRouting("/**/b/c/d", order = 3),
            mockRouting("/:user/:name/c/d", order = 2)
        )
        every { request.path() } returns "/a/b/c/d"

        assertEquals(listOf("/**/c/d", "/**/d", "/:user/:name/c/d", "/**/b/c/d", "/**"),
            RoutingMatcher(routingManager).match(request).getMatchedResults(HANDLER).map { it.routing.path })
    }

    @Test
    fun `throw exceptions if not only double star`() {
        every { routingManager.getRouting(HANDLER) } returns listOf(mockRouting("/**a"))
        assertThrows<AssertionError> {
            RoutingMatcher(routingManager)
        }
    }


    @Test
    fun `get 404 if not found`() {
        every { routingManager.getRouting(HANDLER) } returns listOf(
            mockRouting("/**/d", order = 1),
            mockRouting("/**/c/d", order = 0),
            mockRouting("/**/b/c/d", order = 3),
            mockRouting("/:user/:name/c/d", order = 2)
        )
        every { request.path() } returns "/index.html"

        val results = RoutingMatcher(routingManager).match(request)
        assertEquals(HttpStatusCode.NOT_FOUND, results.handlerMatchResult.statusCode)
        assertTrue(results.handlerMatchResult.exactlyMatchedHandlers.isEmpty())
    }

    @Test
    fun `get 405 if method not allowed`() {
        every { routingManager.getRouting(PRE_HANDLER) } returns listOf(mockRouting("/**", type = PRE_HANDLER))
        every { routingManager.getRouting(POST_HANDLER) } returns listOf(mockRouting("/**", type = POST_HANDLER))

        every { routingManager.getRouting(HANDLER) } returns listOf(
            mockRouting("/user", method = POST),
            mockRouting("/user/:userId", method = GET),
            mockRouting("/user/:userId", method = PATCH),
            mockRouting("/user/:userId", method = DELETE)
        )

        every { request.path() } returns "/user/12345"
        every { request.method() } returns io.vertx.core.http.HttpMethod.POST

        val results = RoutingMatcher(routingManager).match(request)
        assertEquals(HttpStatusCode.METHOD_NOT_ALLOWED, results.handlerMatchResult.statusCode)
        assertTrue(results.handlerMatchResult.exactlyMatchedHandlers.isEmpty())
    }


    @Test
    fun `can process if handler accepts multiple methods`() {
        every { routingManager.getRouting(HANDLER) } returns listOf(
            mockRouting("/user", method = POST),
            mockRouting("/user/:userId", methods = listOf(GET, POST, PATCH, DELETE))
        )

        every { request.path() } returns "/user/12345"
        every { request.method() } returns io.vertx.core.http.HttpMethod.POST

        assertEquals(HttpStatusCode.OK, RoutingMatcher(routingManager).match(request).handlerMatchResult.statusCode)
        assertEquals("/user/:userId", RoutingMatcher(routingManager).match(request).handlerMatchResult.exactlyMatchedHandlers.first().path)
    }

    @Test
    fun `get 406 if not able to fulfil Accept`() {
        every { routingManager.getRouting(PRE_HANDLER) } returns listOf(mockRouting("/**", type = PRE_HANDLER))
        every { routingManager.getRouting(POST_HANDLER) } returns listOf(mockRouting("/**", type = POST_HANDLER))

        every { routingManager.getRouting(HANDLER) } returns listOf(
            mockRouting("/user/:userId", produces = "application/json"),
            mockRouting("/user/:userId", produces = "application/xml"),
            mockRouting("/user/:userId", produces = "application/*")
        )

        every { request.path() } returns "/user/12345"
        every { request.getHeader(OptimizedHeaders.HEADER_ACCEPT) } returns "text/html"

        val results = RoutingMatcher(routingManager).match(request)
        assertEquals("/**", results.getMatchedResults(PRE_HANDLER).first().routing.path)
        assertEquals("/**", results.getMatchedResults(POST_HANDLER).first().routing.path)
        assertEquals(HttpStatusCode.NOT_ACCEPTABLE, results.handlerMatchResult.statusCode)
        assertTrue(results.handlerMatchResult.exactlyMatchedHandlers.isEmpty())
    }

    @Test
    fun `can process if handler produces wildcard`() {
        every { routingManager.getRouting(PRE_HANDLER) } returns listOf(mockRouting("/**", type = PRE_HANDLER))
        every { routingManager.getRouting(POST_HANDLER) } returns listOf(mockRouting("/**", type = POST_HANDLER))

        every { routingManager.getRouting(HANDLER) } returns listOf(
            mockRouting("/order/**", consumes = "*/*"),
            mockRouting("/u*r/:userId", produces = "application/json"),
            mockRouting("/user/:userId", produces = "application/xml"),
            mockRouting("/user/:userId", produces = "application/*"),
            mockRouting("/user/**", produces = "text/*")
        )

        every { request.path() } returns "/user/12345"
        every { request.getHeader(OptimizedHeaders.HEADER_ACCEPT) } returns "text/html"

        val results = RoutingMatcher(routingManager).match(request)

        assertEquals("/**", results.getMatchedResults(PRE_HANDLER).first().routing.path)
        assertEquals("/**", results.getMatchedResults(POST_HANDLER).first().routing.path)
        assertEquals(HttpStatusCode.OK, results.handlerMatchResult.statusCode)
        assertEquals(1, results.handlerMatchResult.exactlyMatchedHandlers.size)
        assertEquals("/user/**", results.handlerMatchResult.exactlyMatchedHandlers.first().path)
    }

    @Test
    fun `get 415 if not able to process Content-Type`() {
        every { routingManager.getRouting(PRE_HANDLER) } returns listOf(mockRouting("/**", type = PRE_HANDLER))
        every { routingManager.getRouting(POST_HANDLER) } returns listOf(mockRouting("/**", type = POST_HANDLER))

        every { routingManager.getRouting(HANDLER) } returns listOf(
            mockRouting("/order/**", consumes = "*/*"),
            mockRouting("/**", consumes = "application/xml"),
            mockRouting("/user/**", consumes = "application/json"),
            mockRouting("/user/:userId", consumes = "application/*")
        )

        every { request.path() } returns "/user/12345"
        every { request.getHeader(OptimizedHeaders.HEADER_ACCEPT) } returns "text/html"

        val results = RoutingMatcher(routingManager).match(request)

        assertEquals("/**", results.getMatchedResults(PRE_HANDLER).first().routing.path)
        assertEquals("/**", results.getMatchedResults(POST_HANDLER).first().routing.path)
        assertEquals(HttpStatusCode.UNSUPPORTED_MEDIA_TYPE, results.handlerMatchResult.statusCode)
        assertTrue(results.handlerMatchResult.exactlyMatchedHandlers.isEmpty())
    }

    @Test
    fun `can process if handler consumes wildcard`() {
        every { routingManager.getRouting(PRE_HANDLER) } returns listOf(mockRouting("/**", type = PRE_HANDLER))
        every { routingManager.getRouting(POST_HANDLER) } returns listOf(mockRouting("/**", type = POST_HANDLER))

        every { routingManager.getRouting(HANDLER) } returns listOf(
            mockRouting("/order/**", consumes = "*/*"),
            mockRouting("/**", consumes = "application/xml"),
            mockRouting("/user/**", consumes = "application/json"),
            mockRouting("/**/*/:userId", consumes = "text/*")
        )

        every { request.path() } returns "/user/12345"
        every { request.getHeader(OptimizedHeaders.HEADER_CONTENT_TYPE) } returns "text/html"

        val results = RoutingMatcher(routingManager).match(request)

        assertEquals("/**", results.getMatchedResults(PRE_HANDLER).first().routing.path)
        assertEquals("/**", results.getMatchedResults(POST_HANDLER).first().routing.path)
        assertEquals(HttpStatusCode.OK, results.handlerMatchResult.statusCode)
        assertEquals(1, results.handlerMatchResult.exactlyMatchedHandlers.size)
        assertEquals("/**/*/:userId", results.handlerMatchResult.exactlyMatchedHandlers.first().path)
    }
}