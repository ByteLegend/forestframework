package io.forestframework.core.http.bridge

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.HttpContext
import io.forestframework.core.http.OptimizedHeaders
import io.forestframework.core.http.param.Cookie
import io.forestframework.core.http.result.ResultProcessor
import io.forestframework.core.http.result.RoutingResultProcessor
import io.forestframework.core.http.routing.Get
import io.forestframework.core.http.routing.Routing
import io.forestframework.ext.api.DefaultApplicationContext
import io.forestframework.ext.api.OnEvent
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.core.AutoEventConsumerScanExtension
import io.forestframework.testfixtures.withSystemPropertyConfigFile
import io.forestframework.testsupport.utils.FreePortFinder
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.delay
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.logging.LogType
import org.testcontainers.Testcontainers.exposeHostPorts
import org.testcontainers.containers.BrowserWebDriverContainer
import java.io.File
import java.util.UUID
import java.util.logging.Level
import javax.inject.Singleton

@DisabledOnOs(OS.MAC, OS.WINDOWS)
class BridgeMethodInvocationIntegrationTest {
    companion object {
        @JvmStatic
        lateinit var app: DefaultApplicationContext

        @JvmStatic
        var port: Int = 0

        @JvmStatic
        lateinit var browserWebDriverContainer: BrowserWebDriverContainer<Nothing>

        @JvmStatic
        @BeforeAll
        fun setUp(@TempDir tempDir: File) {
            port = FreePortFinder.findFreeLocalPort()
            exposeHostPorts(port)
            app = withSystemPropertyConfigFile(
                tempDir, """
                http:
                  port: $port
                bridge:
                  /api/v1:
                    inboundPermitteds:
                    - addressRegex: ".*"
                    outboundPermitteds:
                    - addressRegex: ".*"
            """.trimIndent()
            ) {
                Forest.run(BridgeMethodInvocationIntegrationTestApp::class.java)
            } as DefaultApplicationContext

            browserWebDriverContainer = BrowserWebDriverContainer<Nothing>().apply {
                withCapabilities(ChromeOptions())
                withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, null)
            }
            browserWebDriverContainer.start()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            app.close()
            browserWebDriverContainer.stop()
        }
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = ';',
        value = [
            "echo;                  EchoMessage;              null",
            "echoSuspend;           EchoMessageSuspend;       null",
            "returnVoid;            null;                     null",
            "returnVoidSuspend;     null;                     null",
            """mapTransform;          {"a":2,"b":"bala1","c":[{},{},1]}; null""",
            """mapTransformSuspend;   {"a":2,"b":"bala1","c":[{},{},1]}; null""",
            "error;                 null;                     Oops!"
        ]
    )
    fun `client can invoke backend methods via bridge`(
        address: String,
        expectedResult: String,
        expectedErrorMessage: String
    ) {
        browserWebDriverContainer.webDriver.apply {
            get("http://host.testcontainers.internal:$port/index.html")
            Thread.sleep(500)
            findElementById("${address}Button").click()
            Thread.sleep(100)
            assertNoErrorInConsoleLog()
            if (expectedErrorMessage != "null") {
                Assertions.assertEquals(expectedErrorMessage, findElementById("$address-error").text)
            } else {
                Assertions.assertEquals(expectedResult, findElementById(address).text)
            }
        }
    }

    @Test
    fun `client can authenticate with token`() {
        browserWebDriverContainer.webDriver.apply {
            get("http://host.testcontainers.internal:$port/favicon.ico")
            Thread.sleep(100)
            manage().addCookie(org.openqa.selenium.Cookie("USERNAME", "Alice"))
            get("http://host.testcontainers.internal:$port/index.html")
            Thread.sleep(500)
            findElementById("echoNameButton").click()
            Thread.sleep(100)
            assertNoErrorInConsoleLog()
            Assertions.assertEquals("Alice", findElementById("echoName").text)
        }
    }

    private fun WebDriver.assertNoErrorInConsoleLog() {
        manage().logs().get(LogType.BROWSER).getAll().forEach {
            println("${it.level} ${it.message}")
            if (!it.message.contains("/favicon.ico")) {
                Assertions.assertNotEquals(Level.SEVERE, it.level)
            }
        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Extends(ResultProcessor::class)
@ResultProcessor(by = Html.HtmlResultProcessor::class)
annotation class Html {
    @Singleton
    class HtmlResultProcessor : RoutingResultProcessor {
        override fun processResponse(context: HttpContext, routing: Routing, returnValue: Any): Any {
            val response = context.response()
            response.putHeader(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_TEXT_HTML)
            if (returnValue is Buffer) {
                response.writeLater(returnValue)
            } else {
                response.writeLater(returnValue.toString())
            }
            return returnValue
        }
    }
}

@ForestApplication
@WithExtensions(extensions = [AutoEventConsumerScanExtension::class])
class BridgeMethodInvocationIntegrationTestApp {
    private val tokenToUsername = mutableMapOf<String, String>()

    @Get("/index.html")
    @Html
    fun indexHtml(@Cookie("USERNAME") username: String?): String {
        val html = javaClass.getResourceAsStream("/BridgeMethodInvocationIntegrationTestData/index.html").reader().readText()
        val uuid = UUID.randomUUID().toString()
        tokenToUsername[uuid] = username ?: ""

        return html.replace("{TOKEN}", uuid)
    }

    @Bridge("/api/v1")
    suspend fun bridgeEvent(event: BridgeEvent) {
        event.complete(true)
    }

    @OnEvent("protocol.echo.name")
    fun echoName(token: String): String {
        return tokenToUsername.getValue(token)
    }

    @OnEvent("protocol.echo")
    fun echo(message: String): String {
        return message
    }

    @OnEvent("protocol.echo.suspend")
    suspend fun echoSuspend(message: String): String {
        delay(1)
        return echo(message)
    }

    @OnEvent("protocol.return.void")
    fun returnVoid() {
    }

    @OnEvent("protocol.return.void.suspend")
    suspend fun returnVoidSuspend() {
        delay(1)
    }

    @OnEvent("protocol.map.transform")
    fun mapTransform(obj: JsonObject): JsonObject {
        return JsonObject.mapFrom(obj.getMap().entries.map {
            it.key to
                when {
                    it.value is Int -> (it.value as Int) + 1
                    it.value is String -> (it.value as String) + 1
                    it.value is List<*> -> ArrayList<Any>(it.value as List<Any>).apply { add(1) }
                    it.value is JsonArray -> ArrayList((it.value as JsonArray).list).apply { add(1) }
                    else -> throw IllegalArgumentException("Unexpected value: ${it.value}")
                }
        }.toMap())
    }

    @OnEvent("protocol.map.transform.suspend")
    suspend fun mapTransformSuspend(obj: JsonObject): JsonObject {
        delay(1)
        return mapTransform(obj)
    }

    @OnEvent("protocol.error")
    fun error() {
        throw IllegalStateException("Oops!")
    }
}
