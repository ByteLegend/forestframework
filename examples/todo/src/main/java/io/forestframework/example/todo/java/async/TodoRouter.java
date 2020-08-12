package io.forestframework.example.todo.java.async;

import io.forestframework.core.http.Router;
import io.forestframework.core.http.param.JsonRequestBody;
import io.forestframework.core.http.param.PathParam;
import io.forestframework.core.http.result.GetJson;
import io.forestframework.core.http.result.JsonResponseBody;
import io.forestframework.core.http.routing.Delete;
import io.forestframework.core.http.routing.Get;
import io.forestframework.core.http.routing.Patch;
import io.forestframework.core.http.routing.Post;
import io.forestframework.example.todo.java.Todo;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;

import javax.inject.Inject;
import java.util.List;

@Router
public class TodoRouter {
    private final TodoService todoService;

    @Inject
    public TodoRouter(TodoService todoService) {
        this.todoService = todoService;
    }

    @Get("/todos/:todoId")
    @JsonResponseBody(pretty = true, respond404IfNull = true)
    public Future<Todo> handleGetTodo(@PathParam("todoId") String todoId) {
        return todoService.getCertain(todoId).map(optional -> optional.orElse(null));
    }

    @GetJson(value = "/todos", pretty = true)
    public Future<List<Todo>> handleGetAll() {
        return todoService.all();
    }

    @Post("/todos")
    @JsonResponseBody(pretty = true)
    public Future<Todo> handleCreateTodo(@JsonRequestBody Todo todo, HttpServerRequest request) {
        return todoService.insert(Todo.wrapTodo(todo, request));
    }

    @Patch("/todos/:todoId")
    @JsonResponseBody(pretty = true, respond404IfNull = true)
    public Future<Todo> handleUpdateTodo(@PathParam("todoId") String todoId, @JsonRequestBody Todo todo) {
        return todoService.update(todoId, todo).map(optional -> optional.orElse(null));
    }


    @Delete("/todos/:todoId")
    public Future<Void> handleDeleteOne(@PathParam("todoId") String todoId) {
        return todoService.delete(todoId);
    }

    @Delete("/todos")
    public Future<Void> handleDeleteAll() {
        return todoService.deleteAll();
    }
}
