package io.forestframework.example.todo.java.sync;

import io.forestframework.core.SingletonRouter;
import io.forestframework.core.http.Blocking;
import io.forestframework.core.http.param.JsonRequestBody;
import io.forestframework.core.http.param.PathParam;
import io.forestframework.core.http.result.GetJson;
import io.forestframework.core.http.result.JsonResponseBody;
import io.forestframework.core.http.routing.Delete;
import io.forestframework.core.http.routing.Get;
import io.forestframework.core.http.routing.Patch;
import io.forestframework.core.http.routing.Post;
import io.forestframework.example.todo.java.Todo;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;
import java.util.List;

/**
 * For demonstration only.
 *
 * Try avoid using @Blocking, this has bad impacts on throughput.
 */
@SingletonRouter
@Blocking
public class TodoRouter {
    private final TodoService todoService;

    @Inject
    public TodoRouter(TodoService todoService) {
        this.todoService = todoService;
    }

    @Get("/todos/:todoId")
    @JsonResponseBody(pretty = true)
    public Todo handleGetTodo(@PathParam("todoId") String todoId) {
        return todoService.getCertain(todoId).orElse(null);
    }

    @GetJson(value = "/todos", pretty = true)
    public List<Todo> handleGetAll() {
        return todoService.all();
    }

    @Post("/todos")
    @JsonResponseBody(pretty = true)
    public Todo handleCreateTodo(@JsonRequestBody Todo todo, RoutingContext routingContext) {
        return todoService.insert(Todo.wrapTodo(todo, routingContext));
    }

    @Patch("/todos/:todoId")
    @JsonResponseBody(pretty = true)
    public Todo handleUpdateTodo(@PathParam("todoId") String todoId, @JsonRequestBody Todo todo) {
        return todoService.update(todoId, todo).orElse(null);
    }


    @Delete("/todos/:todoId")
    public void handleDeleteOne(@PathParam("todoId") String todoId) {
        todoService.delete(todoId);
    }

    @Delete("/todos")
    public void handleDeleteAll() {
        todoService.deleteAll();
    }
}
