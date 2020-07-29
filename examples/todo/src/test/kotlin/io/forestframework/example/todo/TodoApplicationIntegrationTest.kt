package io.forestframework.example.todo

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.forestframework.core.config.Config
import io.forestframework.core.http.OptimizedHeaders
import io.forestframework.example.todo.java.async.jdbc.TodoApplicationJavaAsyncJDBC
import io.forestframework.example.todo.java.async.redis.TodoApplicationJavaAsyncRedis
import io.forestframework.example.todo.java.sync.jdbc.TodoApplicationJavaSyncJDBC
import io.forestframework.example.todo.java.sync.redis.TodoApplicationJavaSyncRedis
import io.forestframework.example.todo.kotlin.Todo
import io.forestframework.example.todo.kotlin.jdbc.TodoApplicationKotlinCoroutinesJDBC
import io.forestframework.example.todo.kotlin.redis.TodoApplicationKotlinCoroutinesRedis
import io.forestframework.ext.core.ExtraConfig
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.impl.headers.HeadersMultiMap
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendBufferAwait
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.Extensions
import se.svt.util.junit5.redis.EmbeddedRedisExtension
import se.svt.util.junit5.redis.REDIS_PORT_PROPERTY
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random


@Extensions(
    ExtendWith(EmbeddedRedisExtension::class),
    ExtendWith(RedisSetUpExtension::class),
    ExtendWith(ForestExtension::class)
)
@ForestTest(appClass = TodoApplicationKotlinCoroutinesRedis::class)
class TodoApplicationRedisKotlinCoroutinesIntegrationTest : TodoApplicationIntegrationTest()

@Extensions(
    ExtendWith(EmbeddedRedisExtension::class),
    ExtendWith(RedisSetUpExtension::class),
    ExtendWith(ForestExtension::class)
)
@ForestTest(appClass = TodoApplicationJavaAsyncRedis::class)
class TodoApplicationJavaAsyncRedisIntegrationTest : TodoApplicationIntegrationTest()

@Extensions(
    ExtendWith(EmbeddedRedisExtension::class),
    ExtendWith(RedisSetUpExtension::class),
    ExtendWith(ForestExtension::class)
)
@ForestTest(appClass = TodoApplicationJavaSyncRedis::class)
class TodoApplicationJavaSyncRedisIntegrationTest : TodoApplicationIntegrationTest()

@ExtendWith(ForestExtension::class)
@ForestTest(appClass = TodoApplicationKotlinCoroutinesJDBC::class)
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:TodoApplicationKotlinCoroutinesJDBCIntegrationTest;DATABASE_TO_UPPER=false"])
class TodoApplicationKotlinCoroutinesJDBCIntegrationTest : TodoApplicationIntegrationTest()

@ExtendWith(ForestExtension::class)
@ForestTest(appClass = TodoApplicationJavaAsyncJDBC::class)
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:TodoApplicationJavaAsyncJDBCIntegrationTest;DATABASE_TO_UPPER=false"])
class TodoApplicationJavaAsyncJDBCIntegrationTest : TodoApplicationIntegrationTest()

@ExtendWith(ForestExtension::class)
@ForestTest(appClass = TodoApplicationJavaSyncJDBC::class)
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:TodoApplicationJavaSyncJDBCIntegrationTest;DATABASE_TO_UPPER=false"])
class TodoApplicationJavaSyncJDBCIntegrationTest : TodoApplicationIntegrationTest()


abstract class TodoApplicationIntegrationTest {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Inject
    @Config("forest.http.port")
    lateinit var port: Integer

    @Inject
    lateinit var vertx: Vertx

    lateinit var client: WebClient

    val objectMapper = ObjectMapper()

    val headers = HeadersMultiMap().apply {
        add(OptimizedHeaders.HEADER_ACCEPT, OptimizedHeaders.CONTENT_TYPE_APPLICATION_JSON)
        add(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_APPLICATION_JSON)
    }

    @BeforeEach
    fun setUp() {
        client = WebClient.create(vertx)
    }

    private fun <T> String.toObject(klass: Class<T>) = objectMapper.readValue(this, klass)

    private fun <T> String.toObject(klass: TypeReference<T>) = objectMapper.readValue(this, klass)

    private fun HttpResponse<Buffer>.assert2XXStatus(): HttpResponse<Buffer> {
        assertTrue(statusCode() in 200..299) { "Status: ${statusCode()}" }
        return this
    }

    private fun HttpResponse<Buffer>.assert404(): HttpResponse<Buffer> {
        assertEquals(404, statusCode())
        return this
    }

    private suspend fun createTodo(): Todo {
        val order = Random.nextInt()
        val title = UUID.randomUUID().toString()
        val todo = Todo(0, title, false, order, null)
        return client.post(port.toInt(), "localhost", todoUri())
            .putHeaders(headers)
            .sendBufferAwait(Buffer.buffer(objectMapper.writeValueAsString(todo)))
            .assert2XXStatus()
            .bodyAsString()
            .toObject(Todo::class.java)
    }

    private fun todoUri(id: Int? = null) = "/todos${if (id == null) "" else "/${id}"}"

    @Test
    fun `insert then fetch`() = runBlocking {
        val todo = createTodo()
        val getTodo = client.get(port.toInt(), "localhost", todoUri(todo.id))
            .putHeaders(headers)
            .sendAwait()
            .assert2XXStatus()
            .bodyAsString()
            .toObject(Todo::class.java)
        assertEquals(todo, getTodo)
    }

    @Test
    fun `insert then fetch all`() = runBlocking {
        val todo1 = createTodo()
        val todo2 = createTodo()
        val allTodos = client.get(port.toInt(), "localhost", todoUri())
            .putHeaders(headers)
            .sendAwait()
            .assert2XXStatus()
            .bodyAsString()
            .toObject(object : TypeReference<List<Todo>>() {})
        assertEquals(setOf(todo1, todo2), allTodos.toSet())
    }

    @Test
    fun `insert then update`() = runBlocking {
        val todo = createTodo()
        val copy = Todo(todo.id, UUID.randomUUID().toString(), true, todo.order, todo.url)
        val updated = client.patch(port.toInt(), "localhost", todoUri(copy.id))
            .putHeaders(headers)
            .sendBufferAwait(Buffer.buffer(objectMapper.writeValueAsString(copy)))
            .assert2XXStatus()
            .bodyAsString()
            .toObject(Todo::class.java)

        val todoAgain = client.get(port.toInt(), "localhost", todoUri(todo.id))
            .putHeaders(headers)
            .sendAwait()
            .assert2XXStatus()
            .bodyAsString()
            .toObject(Todo::class.java)
        assertEquals(copy, updated)
        assertEquals(todoAgain, updated)
    }

    @Test
    fun `insert then delete`() = runBlocking<Unit> {
        val todo = createTodo()
        client.delete(port.toInt(), "localhost", todoUri(todo.id))
            .putHeaders(headers)
            .sendAwait()
            .assert2XXStatus()
        client.get(port.toInt(), "localhost", todoUri(todo.id))
            .putHeaders(headers)
            .sendAwait()
            .assert404()
    }

    @Test
    fun `insert then delete all`() = runBlocking {
        createTodo()
        createTodo()
        client.delete(port.toInt(), "localhost", todoUri())
            .putHeaders(headers)
            .sendAwait()
            .assert2XXStatus()
        val allTodos = client.get(port.toInt(), "localhost", todoUri())
            .putHeaders(headers)
            .sendAwait()
            .assert2XXStatus()
            .bodyAsString()
            .toObject(object : TypeReference<List<Todo>>() {})
        assertTrue(allTodos.isEmpty())
    }
}

class RedisSetUpExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        System.setProperty("forest.redis.endpoints", "[\"redis://localhost:${System.getProperty(REDIS_PORT_PROPERTY)}\"]")
    }
}

