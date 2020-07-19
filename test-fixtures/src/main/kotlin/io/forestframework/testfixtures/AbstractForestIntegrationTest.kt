package io.forestframework.testfixtures

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.forestframework.core.config.Config
import io.forestframework.core.http.HttpStatusCode
import io.forestframework.core.http.OptimizedHeaders
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.impl.headers.VertxHttpHeaders
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class AbstractForestIntegrationTest {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Inject
    @Config("forest.http.port")
    lateinit var port: Integer

    @Inject
    lateinit var vertx: Vertx

    lateinit var client: WebClient

    val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        client = WebClient.create(vertx)
    }

    fun post(uri: String) = client.post(port.toInt(), "localhost", uri)

    fun get(uri: String) = client.get(port.toInt(), "localhost", uri)

    fun HttpRequest<Buffer>.contentTypeJson() = VertxHttpHeaders().apply {
        add(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_APPLICATION_JSON)
    }.apply {
        this@contentTypeJson.putHeaders(this)
    }

    fun HttpResponse<Buffer>.assert2XXStatus(): HttpResponse<Buffer> {
        Assertions.assertTrue(statusCode() in 200..299)
        return this
    }

    fun HttpResponse<Buffer>.assert200() = assertStatusCode(HttpStatusCode.OK)

    fun HttpResponse<Buffer>.assert404() = assertStatusCode(HttpStatusCode.NOT_FOUND)

    fun HttpResponse<Buffer>.assertStatusCode(statusCode: HttpStatusCode): HttpResponse<Buffer> {
        Assertions.assertEquals(statusCode.code, statusCode())
        return this
    }

    fun <T> HttpResponse<Buffer>.toObject(klass: Class<T>) = objectMapper.readValue(bodyAsString(), klass)

    fun <T> HttpResponse<Buffer>.toObject(klass: TypeReference<T>) = objectMapper.readValue(bodyAsString(), klass)

    fun HttpResponse<Buffer>.assertContentType(type: String): HttpResponse<Buffer> {
        Assertions.assertEquals(type, getHeader(OptimizedHeaders.HEADER_CONTENT_TYPE.toString()).toString())
        return this
    }

    fun HttpResponse<Buffer>.assertContentType(type: CharSequence):HttpResponse<Buffer> {
        assertContentType(type.toString())
        return this
    }

    fun runBlockingUnit(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit) = runBlocking(context, block)
}

