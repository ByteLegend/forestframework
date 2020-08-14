package io.forestframework.testfixtures

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.forestframework.core.config.Config
import io.forestframework.core.http.HttpStatusCode
import io.forestframework.core.http.OptimizedHeaders
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.WebSocket
import io.vertx.core.http.impl.headers.HeadersMultiMap
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.kotlin.core.http.bodyAwait
import io.vertx.kotlin.core.http.closeAwait
import io.vertx.kotlin.core.http.webSocketAwait
import io.vertx.kotlin.core.http.writeTextMessageAwait
import io.vertx.kotlin.coroutines.awaitResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Timeout
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class HttpClientResponseWrapper(private val delegate: HttpClientResponse) : HttpClientResponse by delegate {
    lateinit var body: Buffer
}

class WebSocketClient(val socket: WebSocket, val url: String) {
    var cursorIndex: Int = 0
    private val receivedMessages = mutableListOf<String>()

    init {
        socket.textMessageHandler {
            println("${System.nanoTime()} $url received message: $it")
            receivedMessages.add(it)
        }
    }

    suspend fun sendMessage(message: String): WebSocketClient {
        socket.writeTextMessageAwait(message)
        return this
    }

    suspend fun waitFor(vararg message: String, timeoutMillis: Int = 5000, stepMillis: Int = 100): WebSocketClient {
        return waitFor(message.toList(), timeoutMillis, stepMillis)
    }

    suspend fun waitFor(message: List<String>, timeoutMillis: Int = 5000, stepMillis: Int = 100): WebSocketClient {
        var millis = 0
        while (millis < timeoutMillis) {
            val indexOfSubList = Collections.indexOfSubList(receivedMessages.subList(cursorIndex, receivedMessages.size), message)
            if (indexOfSubList != -1) {
                cursorIndex += message.size
                return this
            }

            delay(stepMillis.toLong())
            millis += stepMillis
        }

        throw IllegalStateException("Wait for '$message' timeout after $timeoutMillis ms, received messages: $receivedMessages")
    }

    suspend fun close() = socket.closeAwait()
}

@Timeout(value = 30, unit = TimeUnit.SECONDS)
abstract class AbstractForestIntegrationTest {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Inject
    @Config("forest.http.port")
    lateinit var port: Integer

    @Inject
    lateinit var vertx: Vertx

    lateinit var client: HttpClient

    val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        client = vertx.createHttpClient()
    }

    suspend fun HttpClientResponse.bodyAsString(): String =
        if (this is HttpClientResponseWrapper) {
            body.toString()
        } else {
            bodyAwait().toString()
        }

    suspend fun HttpClientResponse.bodyAsBinary(): Buffer =
        if (this is HttpClientResponseWrapper) {
            body
        } else {
            bodyAwait()
        }

    suspend fun openWebsocket(uri: String) = WebSocketClient(client.webSocketAwait(port.toInt(), "localhost", uri), uri)

    // Set body handler before reading the whole response, otherwise
    // https://stackoverflow.com/questions/57957767/illegalstateexception-thrown-when-reading-the-vert-x-http-client-response-body
    suspend fun get(uri: String) = awaitResult<HttpClientResponse> { handler ->
        client.get(port.toInt(), "localhost", uri) { responseAsyncResult ->
            val wrapper = HttpClientResponseWrapper(responseAsyncResult.result())
            responseAsyncResult.result().bodyHandler {
                wrapper.body = it
                handler.handle(responseAsyncResult.map { wrapper })
            }
        }
    }

    fun HttpRequest<Buffer>.contentTypeJson() = HeadersMultiMap().apply {
        add(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_APPLICATION_JSON)
    }.apply {
        this@contentTypeJson.putHeaders(this)
    }

    fun HttpResponse<Buffer>.assert2XXStatus(): HttpResponse<Buffer> {
        Assertions.assertTrue(statusCode() in 200..299)
        return this
    }

    fun HttpClientResponse.assert200() = assertStatusCode(HttpStatusCode.OK)

    fun HttpClientResponse.assert404() = assertStatusCode(HttpStatusCode.NOT_FOUND)

    fun HttpClientResponse.assertStatusCode(statusCode: HttpStatusCode): HttpClientResponse {
        Assertions.assertEquals(statusCode.code, statusCode())
        return this
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun <T> HttpClientResponseWrapper.toObject(klass: Class<T>) = objectMapper.readValue(bodyAsString(), klass)

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun <T> HttpClientResponseWrapper.toObject(klass: TypeReference<T>) = objectMapper.readValue(bodyAsString(), klass)

    fun HttpClientResponse.assertContentType(type: String): HttpClientResponse {
        Assertions.assertEquals(type, getHeader(OptimizedHeaders.HEADER_CONTENT_TYPE.toString()).toString())
        return this
    }

    fun HttpClientResponse.assertContentType(type: CharSequence): HttpClientResponse {
        assertContentType(type.toString())
        return this
    }

    fun runBlockingUnit(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit) = runBlocking(context, block)
}

