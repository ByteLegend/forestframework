package io.forestframework.example.todo.kotlin

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.inject.Injector
import io.forestframework.SingletonComponent
import io.forestframework.core.http.param.JsonRequestBody
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.result.JsonResponseBody
import io.forestframework.core.http.routing.Delete
import io.forestframework.core.http.routing.Get
import io.forestframework.core.http.routing.Patch
import io.forestframework.core.http.routing.Post
import io.forestframework.core.http.staticresource.StaticResource
import io.forestframework.ext.api.Extension
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.runBlocking
import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

interface TodoService {
    suspend fun initData()
    suspend fun insert(todo: Todo): Todo
    suspend fun all(): List<Todo>
    suspend fun getCertain(todoID: String): Optional<Todo>
    suspend fun update(todoId: String, newTodo: Todo): Optional<Todo>
    suspend fun delete(todoId: String)
    suspend fun deleteAll()
}

class InitDataExtension : Extension {
    override fun afterInjector(injector: Injector) {
        runBlocking {
            injector.getInstance(TodoService::class.java).initData()
        }
    }
}

@SingletonComponent
class TodoRouter @Inject constructor(private val service: TodoService) {
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

val counter = AtomicInteger(0)

fun wrapTodo(todo: Todo, routingContext: RoutingContext): Todo {
    todo.id = counter.incrementAndGet()
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
