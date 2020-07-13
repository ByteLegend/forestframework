package io.forestframework.example.todo

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.forestframework.core.config.Config
import io.forestframework.core.http.OptimizedHeaders
import io.forestframework.example.todo.kotlin.TodoApplicationRedis
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.impl.headers.VertxHttpHeaders
import io.vertx.kotlin.core.http.bodyAwait
import io.vertx.kotlin.core.http.deleteAwait
import io.vertx.kotlin.core.http.getAwait
import io.vertx.kotlin.core.http.postAwait
import io.vertx.kotlin.core.http.sendAwait
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


class RedisSetUpExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        System.setProperty("forest.redis.endpoints", "[\"redis://localhost:${System.getProperty(REDIS_PORT_PROPERTY)}\"]")
    }
}

@Extensions(
    ExtendWith(EmbeddedRedisExtension::class),
    ExtendWith(RedisSetUpExtension::class),
    ExtendWith(ForestExtension::class)
)
@ForestTest(appClass = TodoApplicationRedis::class)
class TodoApplicationRedisKotlinIntegrationTest {
    @Inject
    @Config("forest.http.port")
    lateinit var port: Integer

    @Inject
    lateinit var vertx: Vertx

    lateinit var client: HttpClient

    val objectMapper = ObjectMapper()

    val headers = VertxHttpHeaders().apply {
        add(OptimizedHeaders.HEADER_ACCEPT, OptimizedHeaders.CONTENT_TYPE_APPLICATION_JSON)
        add(OptimizedHeaders.HEADER_CONTENT_TYPE, OptimizedHeaders.CONTENT_TYPE_APPLICATION_JSON)
    }

    @BeforeEach
    fun setUp() {
        client = vertx.createHttpClient()
    }

    data class Todo(
        @JsonProperty("id")
        var id: Int,
        @JsonProperty("title")
        val title: String?,
        @JsonProperty("completed")
        val completed: Boolean = false,
        @JsonProperty("order")
        val order: Int?,
        @JsonProperty("url")
        var url: String?
    )

    fun <T> Buffer.toObject(klass: Class<T>) = objectMapper.readValue(this.toString(), klass)

    fun <T> Buffer.toObject(klass: TypeReference<T>) = objectMapper.readValue(this.toString(), klass)

    fun HttpClientResponse.assert2XXStatus(): HttpClientResponse {
        assertTrue(statusCode() in 200..299)
        return this
    }

    fun HttpClientResponse.assert404(): HttpClientResponse {
        assertEquals(404, statusCode())
        return this
    }

    suspend fun createTodo(): Todo {
        val order = Random.nextInt()
        val title = UUID.randomUUID().toString()
        val todo = Todo(0, title, false, order, null)
        return runBlocking {
            client.postAwait("http://localhost:$port/todos", headers, Buffer.buffer(objectMapper.writeValueAsString(todo)))
                .assert2XXStatus()
                .bodyAwait()
                .toObject(Todo::class.java)
        }
    }

    fun todoUrl(id: Int? = null) = "http://localhost:$port/todos${if (id == null) "" else "/${id}"}"

    @Test
    fun `insert then fetch`() = runBlocking {
        val todo = createTodo()
        val getTodo = client.getAwait(todoUrl(todo.id))
            .assert2XXStatus()
            .bodyAwait()
            .toObject(Todo::class.java)
        assertEquals(todo, getTodo)
    }

    @Test
    fun `insert then fetch all`() = runBlocking {
        val todo1 = createTodo()
        val todo2 = createTodo()
        val allTodos = client.getAwait(todoUrl())
            .assert2XXStatus()
            .bodyAwait()
            .toObject(object : TypeReference<List<Todo>>() {})
        assertEquals(listOf(todo1, todo2), allTodos)
    }

    @Test
    fun `insert then update`() = runBlocking {
        val todo = createTodo()
        val copy = Todo(todo.id, UUID.randomUUID().toString(), true, todo.order, todo.url)
        val updated = client.sendAwait(HttpMethod.PATCH, todoUrl(copy.id), headers, Buffer.buffer(objectMapper.writeValueAsString(copy)))
            .assert2XXStatus()
            .bodyAwait()
            .toObject(Todo::class.java)
        assertEquals(copy, updated)
    }

    @Test
    fun `insert then delete`() = runBlocking {
        val todo = createTodo()
        client.deleteAwait(todoUrl(todo.id)).assert2XXStatus()
        client.getAwait(todoUrl(todo.id)).assert404()
    }

    @Test
    fun `insert then delete all`() = runBlocking {
        createTodo()
        createTodo()
        client.deleteAwait(todoUrl()).assert2XXStatus()
        val allTodos = client.getAwait(todoUrl())
            .assert2XXStatus()
            .bodyAwait()
            .toObject(object : TypeReference<List<Todo>>() {})
        assertTrue(allTodos.isEmpty())
    }
}