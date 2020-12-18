package io.forestframework.example.todo.java.async.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.forestframework.core.Component;
import io.forestframework.core.Forest;
import io.forestframework.core.ForestApplication;
import io.forestframework.example.todo.java.async.TodoRouter;
import io.forestframework.example.todo.java.async.TodoService;
import io.forestframework.ext.api.WithExtensions;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.core.IncludeComponents;
import io.forestframework.ext.core.StaticResourceExtension;
import io.forestframework.extensions.jdbc.JDBCClientExtension;
import io.forestframework.utils.completablefuture.VertxCompletableFuture;
import io.vertx.core.Vertx;
import io.vertx.ext.jdbc.JDBCClient;

import static io.forestframework.example.todo.java.async.jdbc.TodoApplicationJavaAsyncJDBC.InitDataExtension;
import static io.forestframework.example.todo.java.async.jdbc.TodoApplicationJavaAsyncJDBC.JDBCModule;

@WithExtensions(extensions = {JDBCClientExtension.class, InitDataExtension.class, StaticResourceExtension.class})
@ForestApplication
@IncludeComponents(classes = {JDBCModule.class, TodoRouter.class})
public class TodoApplicationJavaAsyncJDBC {
    public static void main(String[] args) {
        Forest.run(TodoApplicationJavaAsyncJDBC.class);
    }

    @Component
    public static class JDBCModule extends AbstractModule {
        @Provides
        @Singleton
        public TodoService getTodoService(JDBCClient jdbcClient) {
            return new JDBCTodoService(jdbcClient);
        }
    }

    public static class InitDataExtension implements Extension {
        @Override
        public void configure(Injector injector) {
            try {
                VertxCompletableFuture.from(injector.getInstance(Vertx.class).getOrCreateContext(),
                                            injector.getInstance(TodoService.class).initData()).get();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
