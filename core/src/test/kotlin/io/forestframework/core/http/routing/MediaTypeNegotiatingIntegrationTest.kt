package io.forestframework.core.http.routing

import com.google.inject.Injector
import io.forestframework.core.Forest
import io.forestframework.core.config.ConfigProvider
import io.forestframework.core.http.HttpMethod
import io.forestframework.core.http.routing.RoutingType.HANDLER
import io.forestframework.core.modules.WebRequestHandlingModule
import io.forestframework.ext.api.DefaultStartupContext
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.StartupContext
import io.forestframework.ext.core.HttpServerExtension
import io.forestframework.testfixtures.assertStatusCode
import io.forestframework.testfixtures.delete
import io.forestframework.testfixtures.get
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.BindFreePortExtension
import io.vertx.core.Vertx
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

data class ProducesAcceptCase(
    val description: String,
    val produces: List<String> = listOf("*/*"),
    val accept: String = "*/*",
    val expectedStatusCode: Int = 200,
    val httpMethod: HttpMethod = HttpMethod.GET
)

data class ConsumesContentTypeCase(
    val description: String,
    val consumes: List<String> = listOf("*/*"),
    val contentType: String = "*/*",
    val expectedStatusCode: Int = 200,
    val httpMethod: HttpMethod = HttpMethod.GET
)

data class TestMethodGet(
    val description: String,
    val consumes: List<String> = listOf("*/*"),
    val contentType: String = "*/*",
    val expectedStatusCode: Int = 200,
    val httpMethod: HttpMethod = HttpMethod.GET
)

data class TestMethodDelete(
    val description: String,
    val consumes: List<String> = listOf("*/*"),
    val contentType: String = "*/*",
    val expectedStatusCode: Int = 200,
    val httpMethod: HttpMethod = HttpMethod.GET
)

// Put your test cases here
val producesAcceptCases = listOf(
    ProducesAcceptCase("get 200 when matching single", produces = listOf("application/json"), accept = "application/json", expectedStatusCode = 200),
    ProducesAcceptCase("get 406 when not matching single", produces = listOf("application/json"), accept = "text/html", expectedStatusCode = 406),

    ProducesAcceptCase("get 200 when matching multiple", produces = listOf("application/json", "text/plain"), accept = "text/html, application/json;q=0.9, text/plain;q=0.8", expectedStatusCode = 200),
    ProducesAcceptCase("get 406 when not matching multiple", produces = listOf("application/json", "text/plain"), accept = "application/xhtml+xml, text/html;q=0.3", expectedStatusCode = 406),

    ProducesAcceptCase("get 200 when matching wildcard", produces = listOf("text/*"), accept = "text/html, application/json; level=1;q=0.9, text/plain;q=0.8", expectedStatusCode = 200),
    ProducesAcceptCase("get 406 when not matching wildcard", produces = listOf("application/json"), accept = "text/*", expectedStatusCode = 406),

    ProducesAcceptCase("get 200 when matching double wildcard case 1", produces = listOf("*/*"), accept = "*/*;level=1;q=0.9", expectedStatusCode = 200),
    ProducesAcceptCase("get 200 when matching double wildcard case 2", produces = listOf("text/*"), accept = "*/*;q=0.7", expectedStatusCode = 200),
    ProducesAcceptCase("get 200 when matching double wildcard case 3", produces = listOf("*/*"), accept = "text/*", expectedStatusCode = 200),
    ProducesAcceptCase("get 200 when matching double wildcard case 4", produces = listOf("*/*"), accept = "application/xhtml+xml;q=0.7, image/jxr;level=1", expectedStatusCode = 200),

    ProducesAcceptCase("get 200 when matching with parameter", produces = listOf("application/json;charset=UTF-8"), accept = "application/json;q=0.3", expectedStatusCode = 200),
    ProducesAcceptCase("get 406 when not matching with parameter", produces = listOf("application/json;charset=UTF-8"), accept = "application/json;level=1;charset=UTF-16", expectedStatusCode = 406)
)

val consumesContentTypeCases = listOf(
    ConsumesContentTypeCase("get 200 when matching single", consumes = listOf("application/json"), contentType = "application/json", expectedStatusCode = 200),
    ConsumesContentTypeCase("get 415 when not matching single", consumes = listOf("application/json"), contentType = "text/html", expectedStatusCode = 415),

    ConsumesContentTypeCase("get 200 when matching wildcard", consumes = listOf("application/json", "image/jxr"), contentType = "application/*", expectedStatusCode = 200),
    ConsumesContentTypeCase("get 415 when not matching wildcard", consumes = listOf("application/json"), contentType = "text/*", expectedStatusCode = 415),

    ConsumesContentTypeCase("get 200 when matching double wildcard case 1", consumes = listOf("*/*"), contentType = "application/json", expectedStatusCode = 200),
    ConsumesContentTypeCase("get 200 when matching double wildcard case 2", consumes = listOf("*/*"), contentType = "text/*", expectedStatusCode = 200),

    ConsumesContentTypeCase("get 200 when matching with parameter", consumes = listOf("application/json;charset=UTF-8"), contentType = "application/json;q=0.9", expectedStatusCode = 200),
    ConsumesContentTypeCase("get 415 when not matching with parameter", consumes = listOf("application/json;charset=UTF-8"), contentType = "application/json;charset=UTF-16", expectedStatusCode = 415)
)

val testMethodGet = listOf(
    TestMethodGet("get 200 if handler method is HttpMethod.ALL", httpMethod = HttpMethod.ALL, expectedStatusCode = 200),
    TestMethodGet("get 200 if request method allowed", httpMethod = HttpMethod.GET, expectedStatusCode = 200),
    TestMethodGet("get 405 if request method not allowed", httpMethod = HttpMethod.POST, expectedStatusCode = 405)
)

val testMethodDelete = listOf(
    TestMethodDelete("get 200 if request method allowed", httpMethod = HttpMethod.DELETE, expectedStatusCode = 200),
    TestMethodDelete("get 405 if request method not allowed", httpMethod = HttpMethod.GET, expectedStatusCode = 405),
    TestMethodDelete("get 405 if request method not allowed", httpMethod = HttpMethod.POST, expectedStatusCode = 405)
)

class MediaTypeNegotiatingIntegrationTest {
    @TestFactory
    fun testProducesAccept(): Iterable<DynamicTest> = producesAcceptCases.map(this::testOne)

    private fun testOne(case: ProducesAcceptCase) = DynamicTest.dynamicTest(case.description) {
        runBlockingUnit {
            val vertx = Vertx.vertx()
            val port = startTestApplication(vertx, case.httpMethod, produces = case.produces)

            val httpClient = vertx.createHttpClient()
            httpClient.get(port, "/test", mapOf("Accept" to case.accept)).assertStatusCode(case.expectedStatusCode)
        }
    }

    /**
     * Creates a mock server, registers the routing dynamically, waits for the server startup, then returns the port
     */
    private fun startTestApplication(
        vertx: Vertx,
        httpMethod: HttpMethod = HttpMethod.GET,
        produces: List<String> = listOf("*/*"),
        consumes: List<String> = listOf("*/*")
    ): Int {
        val configProvider = ConfigProvider(HashMap(), HashMap())
        val extensions = listOf(
            BindFreePortExtension(),
            object : Extension {
                override fun beforeInjector(startupContext: StartupContext?) {
                    startupContext!!.componentClasses.add(WebRequestHandlingModule::class.java)
                }

                override fun afterInjector(injector: Injector) {
                    injector.getInstance(RoutingManager::class.java).getRouting(HANDLER).add(
                        DefaultRouting(false,
                            HANDLER,
                            "/test",
                            "",
                            listOf(httpMethod),
                            MediaTypeNegotiatingIntegrationTest::class.java.getMethod("dummy"),
                            0,
                            produces,
                            consumes)
                    )
                }
            },
            HttpServerExtension()
        )
        val context = DefaultStartupContext(vertx, MediaTypeNegotiatingIntegrationTest::class.java, configProvider, extensions)
        Forest.run(context)

        return configProvider.getInstance("forest.http.port", Integer::class.java).toInt()
    }

    @TestFactory
    fun testConsumesContentType() = consumesContentTypeCases.map(this::testOne)

    private fun testOne(case: ConsumesContentTypeCase) = DynamicTest.dynamicTest(case.description) {
        runBlockingUnit {
            val vertx = Vertx.vertx()
            val port = startTestApplication(vertx, case.httpMethod, consumes = case.consumes)

            val httpClient = vertx.createHttpClient()
            httpClient.get(port, "/test", mapOf("Content-Type" to case.contentType)).assertStatusCode(case.expectedStatusCode)
        }
    }

    @TestFactory
    fun testMethodGet() = testMethodGet.map(this::testOne)

    private fun testOne(case: TestMethodGet) = DynamicTest.dynamicTest(case.description) {
        runBlockingUnit {
            val vertx = Vertx.vertx()
            val port = startTestApplication(vertx, httpMethod = case.httpMethod)

            val httpClient = vertx.createHttpClient()
            httpClient.get(port, "/test").assertStatusCode(case.expectedStatusCode)
        }
    }

    @TestFactory
    fun testMethodDelete() = testMethodDelete.map(this::testOne)

    private fun testOne(case: TestMethodDelete) = DynamicTest.dynamicTest(case.description) {
        runBlockingUnit {
            val vertx = Vertx.vertx()
            val port = startTestApplication(vertx, httpMethod = case.httpMethod)

            val httpClient = vertx.createHttpClient()
            httpClient.delete(port, "/test").assertStatusCode(case.expectedStatusCode)
        }
    }

    fun dummy() {}
}
