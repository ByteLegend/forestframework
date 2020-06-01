package org.forestframework

import com.google.inject.AbstractModule
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.ext.sql.executeAwait
import io.vertx.kotlin.ext.sql.getConnectionAwait
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import io.vertx.redis.client.Redis
import org.forestframework.annotation.Delete
import org.forestframework.annotation.ForestApplication
import org.forestframework.annotation.Get
import org.forestframework.annotation.JsonResponseBody
import org.forestframework.annotation.Patch
import org.forestframework.annotation.PathParam
import org.forestframework.annotation.Post
import org.forestframework.annotation.RequestBody
import org.forestframework.extensions.jdbc.JDBCClientExtension
import org.forestframework.extensions.redis.RedisClientExtension
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@ForestApplication(
    include = [JDBCClientExtension::class, RedisClientExtension::class]
)
class ToDoApplication

class ServiceSelector : AbstractModule() {
    override fun configure() {
    }
}

fun main() {
    Forest.run(ToDoApplication::class.java)
}

@Singleton
class ToDoRouter @Inject constructor(val service: TodoService) {
    @Get("/todos/:todoId")
    @JsonResponseBody(pretty = true)
    suspend fun handleGetTodo(@PathParam("todoId") todoID: String) = service.getCertain(todoID)

    @Get("/todos")
    @JsonResponseBody(pretty = true)
    suspend fun handleGetAll() = service.all()

    @Post("/todos")
    suspend fun handleCreateTodo(@RequestBody todo: Todo) = service.insert(todo)

    @Patch("/todos/:todoId")
    suspend fun handleUpdateTodo(@PathParam("todoId") todoId: String, @RequestBody todo: Todo) = service.update(todoId, todo)

    @Delete("/todos/:todoId")
    suspend fun handleDeleteOne(@PathParam("todoId") todoID: String) = service.delete(todoID)

    @Delete("/todos")
    suspend fun handleDeleteAll() = service.deleteAll()
}

interface TodoService {
    suspend fun initData()
    suspend fun insert(todo: Todo): Todo
    suspend fun all(): List<Todo>
    suspend fun getCertain(todoID: String): Optional<Todo>
    suspend fun update(todoId: String, newTodo: Todo): Optional<Todo>
    suspend fun delete(todoId: String)
    suspend fun deleteAll()
}

class RedisTodoService @Inject constructor(val redis: Redis) : TodoService {
    override suspend fun initData() {
        TODO("Not yet implemented")
    }

    override suspend fun insert(todo: Todo): Todo {
        TODO("Not yet implemented")
    }

    override suspend fun all(): List<Todo> {
        TODO("Not yet implemented")
    }

    override suspend fun getCertain(todoID: String): Optional<Todo> {
        TODO("Not yet implemented")
    }

    override suspend fun update(todoId: String, newTodo: Todo): Optional<Todo> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(todoId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }
}

@Singleton
class JdbcTodoService @Inject constructor(val jdbcClient: JDBCClient) : TodoService {
    private val SQL_CREATE = """
CREATE TABLE IF NOT EXISTS `todo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `completed` tinyint(1) DEFAULT NULL,
  `order` int(11) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) )"""
    private val SQL_INSERT = "INSERT INTO `todo` " +
        "(`id`, `title`, `completed`, `order`, `url`) VALUES (?, ?, ?, ?, ?)"

    private val SQL_QUERY_ALL = "SELECT * FROM todo"
    private val SQL_UPDATE = """
        UPDATE `todo`
        SET
        `id` = ?,
        `title` = ?,
        `completed` = ?,
        `order` = ?,
        `url` = ?
        WHERE `id` = ?;
        """.trimIndent()
    private val SQL_DELETE = "DELETE FROM `todo` WHERE `id` = ?"
    private val SQL_DELETE_ALL = "DELETE FROM `todo`"
    private val SQL_QUERY = "SELECT * FROM todo WHERE id = ?"

    override suspend fun initData() {
        jdbcClient.getConnectionAwait().executeAwait(SQL_CREATE)
    }

    override suspend fun insert(todo: Todo): Todo {
        val params = JsonArray().add(todo.id)
            .add(todo.title)
            .add(todo.completed)
            .add(todo.order)
            .add(todo.url)
        jdbcClient.updateWithParamsAwait(SQL_INSERT, params)
        return todo
    }

    override suspend fun all(): List<Todo> {
        return jdbcClient.queryAwait(SQL_QUERY_ALL).rows.map { Todo(it) }
    }

    override suspend fun getCertain(todoID: String): Optional<Todo> {
        return Optional.ofNullable(jdbcClient.queryWithParamsAwait(SQL_QUERY, JsonArray().add(todoID))
            .rows
            .firstOrNull())
            .map { Todo(it) }
    }

    override suspend fun update(todoId: String, newTodo: Todo): Optional<Todo> {
        val target = getCertain(todoId)
        return if (target.isPresent) {
            val fnTodo: Todo = target.get().merge(newTodo)
            val params = JsonArray().add(todoId)
                .add(fnTodo.title)
                .add(fnTodo.completed)
                .add(fnTodo.order)
                .add(fnTodo.url)
                .add(todoId)
            jdbcClient.updateWithParamsAwait(SQL_UPDATE, params)
            target
        } else {
            Optional.empty()
        }
    }

    override suspend fun delete(todoId: String) {
        jdbcClient.updateWithParamsAwait(SQL_DELETE, JsonArray().add(todoId))
    }

    override suspend fun deleteAll() {
        jdbcClient.updateAwait(SQL_DELETE_ALL)
    }

}

data class Todo(val id: Int, val title: String?, val completed: Boolean?, val order: Int?, val url: String?) {
    constructor(jsonObject: JsonObject)
        : this(
        jsonObject.getInteger("id"),
        jsonObject.getString("title"),
        jsonObject.getBoolean("completed"),
        jsonObject.getInteger("order"),
        jsonObject.getString("url")
    )

    fun merge(todo: Todo): Todo {
        return Todo(id,
            todo.title ?: title,
            todo.completed ?: completed,
            todo.order ?: order,
            todo.url ?: url)
    }
}
