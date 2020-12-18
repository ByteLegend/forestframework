package io.forestframework.example.todo.java.sync.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import io.forestframework.core.Forest;
import io.forestframework.core.ForestApplication;
import io.forestframework.example.todo.java.sync.TodoRouter;
import io.forestframework.example.todo.java.sync.TodoService;
import io.forestframework.ext.api.WithExtensions;
import io.forestframework.ext.api.Extension;
import io.forestframework.ext.core.IncludeComponents;
import io.forestframework.ext.core.StaticResourceExtension;

import static io.forestframework.example.todo.java.sync.jdbc.TodoApplicationJavaSyncJDBC.InitDataExtension;
import static io.forestframework.example.todo.java.sync.jdbc.TodoApplicationJavaSyncJDBC.JDBCModule;

@WithExtensions(extensions = {StaticResourceExtension.class, InitDataExtension.class})
@ForestApplication
@IncludeComponents(classes = {TodoRouter.class, JDBCModule.class})
public class TodoApplicationJavaSyncJDBC {
    public static void main(String[] args) {
        Forest.run(TodoApplicationJavaSyncJDBC.class);
    }

    public static class JDBCModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(TodoService.class).to(BlockingJDBCTodoService.class);
        }
    }

    public static class InitDataExtension implements Extension {
        @Override
        public void configure(Injector injector) {
            injector.getInstance(TodoService.class).initData();
        }
    }
}
