package io.forestframework.example.todo.kotlin

import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.extensions.redis.RedisClientExtension
import io.vertx.core.json.Json
import io.vertx.kotlin.redis.client.delAwait
import io.vertx.kotlin.redis.client.hdelAwait
import io.vertx.kotlin.redis.client.hgetAwait
import io.vertx.kotlin.redis.client.hsetAwait
import io.vertx.kotlin.redis.client.hvalsAwait
import io.vertx.redis.client.RedisAPI
import java.util.Optional
import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@ForestApplication(
    include = [RedisModule::class],
    extensions = [RedisClientExtension::class]
)
class TodoApplicationRedis

fun main() {
    Forest.run(TodoApplicationRedis::class.java)
}

class RedisModule : AbstractModule() {
    @Provides
    @Singleton
    fun configure(redisClient: RedisAPI): TodoService = RedisTodoService(redisClient)
}


@Singleton
class RedisTodoService @Inject constructor(private val client: RedisAPI) : TodoService {
    private val redisToDoKey = "VERT_TODO"
    override suspend fun initData() {
        val sample = Todo(abs(ThreadLocalRandom.current().nextInt(0, Int.MAX_VALUE)),
            "Something to do...", false, 1, "todo/ex")
        insert(sample)
    }

    override suspend fun insert(todo: Todo): Todo {
        val encoded = Json.encodePrettily(todo)
        client.hsetAwait(listOf(redisToDoKey, todo.id.toString(), encoded))
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
