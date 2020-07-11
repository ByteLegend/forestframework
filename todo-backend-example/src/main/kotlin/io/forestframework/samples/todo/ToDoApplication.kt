package io.forestframework.samples.todo

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.param.JsonRequestBody
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.result.JsonResponseBody
import io.forestframework.core.http.routing.Delete
import io.forestframework.core.http.routing.Get
import io.forestframework.core.http.routing.Patch
import io.forestframework.core.http.routing.Post
import io.forestframework.core.http.staticresource.StaticResource
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.StartupContext
import io.forestframework.extensions.jdbc.JDBCClientExtension
import io.forestframework.extensions.redis.RedisClientExtension
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.ext.sql.executeAwait
import io.vertx.kotlin.ext.sql.getConnectionAwait
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import io.vertx.kotlin.redis.client.delAwait
import io.vertx.kotlin.redis.client.hdelAwait
import io.vertx.kotlin.redis.client.hgetAwait
import io.vertx.kotlin.redis.client.hsetAwait
import io.vertx.kotlin.redis.client.hvalsAwait
import io.vertx.redis.client.RedisAPI
import kotlinx.coroutines.runBlocking
import java.util.Optional
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
@ForestApplication(
    extensions = [
        ServiceSelectorExtension::class
    ]
)
class ToDoApplication @Inject constructor(private val service: TodoService) {
    @Get("/index.html")
    @StaticResource(webroot = "static")
    fun index() = "/index.html"

    @Get(regex = """\/(?<dir>(js|css))\/(?<file>.+)""")
    @StaticResource
    fun jsCss(@PathParam("dir") dir: String, @PathParam("file") file: String) = "/$dir/$file"

    @Get("/todos/:todoId")
    @JsonResponseBody(pretty = true)
    suspend fun handleGetTodo(@PathParam("todoId") todoID: String) = service.getCertain(todoID).orElse(null)

    @Get("/todos")
    @JsonResponseBody(pretty = true)
    suspend fun handleGetAll() = service.all()

    @Post("/todos")
    @JsonResponseBody(pretty = true)
    suspend fun handleCreateTodo(@JsonRequestBody todo: Todo, routingContext: RoutingContext): Todo = service.insert(wrapTodo(todo, routingContext))

    @Patch("/todos/:todoId")
    @JsonResponseBody(pretty = true)
    suspend fun handleUpdateTodo(@PathParam("todoId") todoId: String, @JsonRequestBody todo: Todo) = service.update(todoId, todo).orElse(null)

    @Delete("/todos/:todoId")
    suspend fun handleDeleteOne(@PathParam("todoId") todoID: String) = service.delete(todoID)

    @Delete("/todos")
    suspend fun handleDeleteAll() = service.deleteAll()
}

fun main() {
    Forest.run(ToDoApplication::class.java)
}

class ServiceSelectorExtension : Extension {
    lateinit var extension: Extension

    override fun afterInjector(injector: Injector) {
        extension.afterInjector(injector)
        runBlocking {
            try {
                injector.getInstance(TodoService::class.java).initData()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun beforeInjector(startupContext: StartupContext) {
        if (startupContext.configProvider.getInstance("serviceType", String::class.java) == "jdbc") {
            extension = JDBCClientExtension()
            startupContext.componentClasses.add(JDBCModule::class.java)
        } else {
            extension = RedisClientExtension()
            startupContext.componentClasses.add(RedisModule::class.java)
        }
        extension.beforeInjector(startupContext)
    }
}

class JDBCModule : AbstractModule() {
    @Provides
    @Singleton
    fun configure(jdbcClient: JDBCClient): TodoService = JdbcTodoService(jdbcClient)
}

class RedisModule : AbstractModule() {
    @Provides
    @Singleton
    fun configure(redisClient: RedisAPI): TodoService = RedisTodoService(redisClient)
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

@Singleton
class JdbcTodoService @Inject constructor(private val jdbcClient: JDBCClient) : TodoService {
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
            Optional.of(fnTodo)
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

val counter = AtomicInteger(0)

fun wrapTodo(todo: Todo, routingContext: RoutingContext): Todo {
    if (todo.id > counter.get()) {
        counter.set(todo.id);
    } else if (todo.id == 0) {
        todo.id = counter.incrementAndGet();
    }
    todo.url = routingContext.request().absoluteURI() + "/" + todo.id
    return todo
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
) {
    constructor(jsonStr: String) : this(JsonObject(jsonStr))

    constructor(jsonObject: JsonObject)
        : this(
        jsonObject.getInteger("id"),
        jsonObject.getString("title"),
        jsonObject.getBoolean("completed") ?: false,
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
