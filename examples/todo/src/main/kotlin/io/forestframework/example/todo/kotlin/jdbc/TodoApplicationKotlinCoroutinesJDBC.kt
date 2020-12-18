package io.forestframework.example.todo.kotlin.jdbc

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import io.forestframework.core.Component
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.example.todo.kotlin.Todo
import io.forestframework.example.todo.kotlin.TodoRouter
import io.forestframework.example.todo.kotlin.TodoService
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.core.IncludeComponents
import io.forestframework.extensions.jdbc.EnableJDBCClient
import io.vertx.core.json.JsonArray
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.ext.sql.executeAwait
import io.vertx.kotlin.ext.sql.getConnectionAwait
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

@ForestApplication
@IncludeComponents(classes = [TodoRouter::class])
@EnableJDBCClient
@WithExtensions(extensions = [InitDataExtension::class])
class TodoApplicationKotlinCoroutinesJDBC

fun main() {
    Forest.run(TodoApplicationKotlinCoroutinesJDBC::class.java)
}

@Component
class JDBCModule : AbstractModule() {
    @Provides
    @Singleton
    fun configure(jdbcClient: JDBCClient): TodoService = JdbcTodoService(jdbcClient)
}

class InitDataExtension : Extension {
    override fun configure(injector: Injector) {
        runBlocking {
            injector.getInstance(TodoService::class.java).initData()
        }
    }
}

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
        WHERE `id` = ?
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
