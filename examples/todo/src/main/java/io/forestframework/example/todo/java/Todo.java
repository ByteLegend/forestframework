package io.forestframework.example.todo.java;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;

public class Todo {

    private static final AtomicInteger ACC = new AtomicInteger(0);

    private int id;
    private String title;
    private Boolean completed;
    private Integer order;
    private String url;

    public Todo() {
    }

    public Todo(Todo other) {
        this.id = other.id;
        this.title = other.title;
        this.completed = other.completed;
        this.order = other.order;
        this.url = other.url;
    }

    public Todo(JsonObject obj) {
        fromJson(obj, this);
    }

    public Todo(String jsonStr) {
        fromJson(new JsonObject(jsonStr), this);
    }

    public Todo(int id, String title, Boolean completed, Integer order, String url) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.order = order;
        this.url = url;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        toJson(this, json);
        return json;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIncId() {
        this.id = ACC.incrementAndGet();
    }

    public static int getIncId() {
        return ACC.get();
    }

    public static void setIncIdWith(int n) {
        ACC.set(n);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean isCompleted() {
        return getOrElse(completed, false);
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Integer getOrder() {
        return getOrElse(order, 0);
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Todo todo = (Todo) o;

        if (id != todo.id) {
            return false;
        }
        if (!title.equals(todo.title)) {
            return false;
        }
        if (completed != null ? !completed.equals(todo.completed) : todo.completed != null) {
            return false;
        }
        return order != null ? order.equals(todo.order) : todo.order == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + title.hashCode();
        result = 31 * result + (completed != null ? completed.hashCode() : 0);
        result = 31 * result + (order != null ? order.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Todo -> {" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", order=" + order +
                ", url='" + url + '\'' +
                '}';
    }

    private <T> T getOrElse(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public Todo merge(Todo todo) {
        return new Todo(id,
                getOrElse(todo.title, title),
                getOrElse(todo.completed, completed),
                getOrElse(todo.order, order),
                url);
    }

    public static void fromJson(JsonObject json, Todo obj) {
        if (json.getValue("completed") instanceof Boolean) {
            obj.setCompleted((Boolean) json.getValue("completed"));
        }
        if (json.getValue("completed") instanceof Number) {
            obj.setCompleted(json.getInteger("completed") != 0);
        }
        if (json.getValue("id") instanceof Number) {
            obj.setId(((Number) json.getValue("id")).intValue());
        }
        if (json.getValue("order") instanceof Number) {
            obj.setOrder(((Number) json.getValue("order")).intValue());
        }
        if (json.getValue("title") instanceof String) {
            obj.setTitle((String) json.getValue("title"));
        }
        if (json.getValue("url") instanceof String) {
            obj.setUrl((String) json.getValue("url"));
        }
    }

    public static void toJson(Todo obj, JsonObject json) {
        if (obj.isCompleted() != null) {
            json.put("completed", obj.isCompleted());
        }
        json.put("id", obj.getId());
        if (obj.getOrder() != null) {
            json.put("order", obj.getOrder());
        }
        if (obj.getTitle() != null) {
            json.put("title", obj.getTitle());
        }
        if (obj.getUrl() != null) {
            json.put("url", obj.getUrl());
        }
    }

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static Todo wrapTodo(Todo todo, HttpServerRequest request) {
        todo.id = COUNTER.incrementAndGet();
        todo.url = request.absoluteURI() + "/" + todo.id;
        return todo;
    }
}
