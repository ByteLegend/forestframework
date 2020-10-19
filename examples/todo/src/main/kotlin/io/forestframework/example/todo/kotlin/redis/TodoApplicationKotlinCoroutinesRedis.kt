package io.forestframework.example.todo.kotlin.redis

import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.forestframework.core.Component
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.example.todo.kotlin.Todo
import io.forestframework.example.todo.kotlin.TodoRouter
import io.forestframework.example.todo.kotlin.TodoService
import io.forestframework.ext.core.IncludeComponents
import io.forestframework.extensions.redis.EnableRedisClient
import io.vertx.core.json.Json
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.redis.client.delAwait
import io.vertx.kotlin.redis.client.hdelAwait
import io.vertx.kotlin.redis.client.hgetAwait
import io.vertx.kotlin.redis.client.hsetAwait
import io.vertx.kotlin.redis.client.hvalsAwait
import io.vertx.redis.client.RedisAPI
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@ForestApplication
@IncludeComponents(classes = [TodoRouter::class])
@EnableRedisClient
class TodoApplicationKotlinCoroutinesRedis

fun main() {
    Forest.run(TodoApplicationKotlinCoroutinesRedis::class.java)
}

@Component
class RedisModule : AbstractModule() {
    @Provides
    @Singleton
    fun configure(redisClient: RedisAPI): TodoService = RedisTodoService(redisClient)
}

class RedisTodoService @Inject constructor(private val client: RedisAPI) : TodoService {
    private val redisToDoKey = "VERT_TODO"

    override suspend fun insert(todo: Todo): Todo {
        val encoded = Json.encodePrettily(todo)
        client.hset(listOf(redisToDoKey, todo.id.toString(), encoded)).await()
        return todo
    }

    override suspend fun all(): List<Todo> = client.hvalsAwait(redisToDoKey)?.map { Todo(it.toString()) } ?: emptyList()

    override suspend fun getCertain(todoID: String): Optional<Todo> {
        val jsonStr = client.hgetAwait(redisToDoKey, todoID)?.toString()
        val todo = if (jsonStr == null) null else Todo(jsonStr)
        return Optional.ofNullable(todo)
    }

    override suspend fun update(todoId: String, newTodo: Todo): Optional<Todo> {
        val old = getCertain(todoId)
        return if (old.isPresent) {
            val result = old.get().merge(newTodo)
            insert(result)
            Optional.of(result)
        } else {
            Optional.empty()
        }
    }

    override suspend fun delete(todoId: String) {
        client.hdelAwait(listOf(redisToDoKey, todoId))
    }

    override suspend fun deleteAll() {
        client.delAwait(listOf(redisToDoKey))
    }
}
