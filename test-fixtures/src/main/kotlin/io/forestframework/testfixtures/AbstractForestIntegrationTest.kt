package io.forestframework.testfixtures

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.forestframework.core.config.Config
import io.forestframework.core.http.HttpMethod
import io.forestframework.core.http.HttpStatusCode
import io.forestframework.core.http.OptimizedHeaders
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.WebSocket
import io.vertx.core.http.WebsocketVersion
import io.vertx.core.http.impl.headers.HeadersMultiMap
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.http.HttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Timeout
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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
        socket.writeTextMessage(message).await()
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

    suspend fun close() = socket.close().await()
}

class HttpClient(private val delegate: CloseableHttpClient) {
    companion object {
        fun create(): HttpClient = HttpClient(HttpClients.createDefault())
    }

    fun get(port: Int, path: String, headers: Map<String, String> = emptyMap()): HttpClientResponse = send(HttpMethod.GET, port, path, headers)

    fun delete(port: Int, path: String, headers: Map<String, String> = emptyMap()): HttpClientResponse = send(HttpMethod.DELETE, port, path, headers)

    fun send(
        httpMethod: HttpMethod,
        port: Int,
        path: String,
        headers: Map<String, String> = emptyMap(),
        body: String = "",
        multiHeaders: Map<String, List<String>> = emptyMap()
    ): HttpClientResponse {
        val request = RequestBuilder.create(httpMethod.name)
            .setUri("http://localhost:$port$path")
            .apply {
                config = RequestConfig.custom().setRedirectsEnabled(false).build()
                headers.forEach(this::setHeader)
                multiHeaders.forEach { (name, values) -> values.forEach { this.addHeader(name, it) } }
                entity = StringEntity(body)
            }
            .build()
        return HttpClientResponse(delegate.execute(request))
    }
}

// Hide HttpClient implementations
class HttpClientResponse(private val delegate: HttpResponse) {
    val objectMapper = ObjectMapper()

    fun bodyAsString() = EntityUtils.toString(delegate.entity)

    fun <T> toObject(klass: Class<T>) = objectMapper.readValue(bodyAsString(), klass)

    fun <T> toObject(klass: TypeReference<T>) = objectMapper.readValue(bodyAsString(), klass)

    fun assertContentType(type: String): HttpClientResponse {
        Assertions.assertEquals(type, delegate.getFirstHeader(OptimizedHeaders.HEADER_CONTENT_TYPE.toString()).value)
        return this
    }

    fun assertStatusCode(statusCode: HttpStatusCode): HttpClientResponse {
        Assertions.assertEquals(statusCode.code, getStatusCode())
        return this
    }

    fun assertStatusCode(statusCode: Int): HttpClientResponse {
        Assertions.assertEquals(statusCode, getStatusCode())
        return this
    }

    fun bodyAsBinary(): Buffer {
        return Buffer.buffer(EntityUtils.toByteArray(delegate.entity))
    }

    fun getStatusCode(): Int = delegate.statusLine.statusCode

    fun assert2XXStatus(): HttpClientResponse {
        Assertions.assertTrue(getStatusCode() in 200..299)
        return this
    }

    fun assert200() = assertStatusCode(HttpStatusCode.OK)

    fun assert500() = assertStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR)

    fun assertBody(expected: String) = bodyAsString().apply {
        Assertions.assertEquals(expected, this)
    }

    fun assert404() = assertStatusCode(HttpStatusCode.NOT_FOUND)

    fun assertHeader(name: String, value: String): HttpClientResponse {
        Assertions.assertEquals(value, delegate.getFirstHeader(name).value)
        return this
    }
}

fun runBlockingUnit(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit) = runBlocking(context, block)

@Timeout(value = 30, unit = TimeUnit.SECONDS)
abstract class AbstractForestIntegrationTest {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Inject
    @Config("forest.http.port")
    lateinit var port: Integer

    @Inject
    lateinit var vertx: Vertx

    val client: HttpClient by lazy {
        HttpClient.create()
    }

    // Known issues, not recommended, for example this one (but not only this one)
    // https://stackoverflow.com/questions/57957767/illegalstateexception-thrown-when-reading-the-vert-x-http-client-response-body
    private val vertxClient: io.vertx.core.http.HttpClient by lazy {
        vertx.createHttpClient()
    }

    fun send(
        httpMethod: HttpMethod,
        path: String,
        headers: Map<String, String> = emptyMap(),
        body: String = "",
        multiHeaders: Map<String, List<String>> = emptyMap()
    ): HttpClientResponse =
        client.send(httpMethod, port.toInt(), path, headers, body, multiHeaders)

    fun get(path: String, headers: Map<String, String> = emptyMap()): HttpClientResponse = send(HttpMethod.GET, path, headers)

    fun post(path: String, headers: Map<String, String> = emptyMap(), body: String = ""): HttpClientResponse = send(HttpMethod.POST, path, headers, body)

    suspend fun openWebsocket(uri: String) = WebSocketClient(vertxClient.webSocket(port.toInt(), "localhost", uri).await(), uri)

    suspend fun openWebsocketAbs(absUrl: String, headers: Map<String, String>) =
        WebSocketClient(
            vertxClient.webSocketAbs(
                absUrl,
                HeadersMultiMap.headers().setAll(headers),
                WebsocketVersion.V13,
                emptyList()).await(),
            absUrl)
}
