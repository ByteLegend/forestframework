package io.forestframework.example

import geb.Browser
import geb.Configuration
import io.forestframework.core.http.Router
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.staticresource.GetStaticResource
import io.forestframework.example.todo.java.async.jdbc.TodoApplicationJavaAsyncJDBC
import io.forestframework.example.todo.java.async.redis.TodoApplicationJavaAsyncRedis
import io.forestframework.example.todo.java.sync.jdbc.TodoApplicationJavaSyncJDBC
import io.forestframework.example.todo.java.sync.redis.TodoApplicationJavaSyncRedis
import io.forestframework.example.todo.kotlin.jdbc.TodoApplicationKotlinCoroutinesJDBC
import io.forestframework.example.todo.kotlin.redis.TodoApplicationKotlinCoroutinesRedis
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.ApplicationContext
import io.forestframework.ext.core.ExtraConfig
import io.forestframework.testfixtures.AbstractBrowserTest
import io.forestframework.testfixtures.EmbeddedRedisExtension
import io.forestframework.testfixtures.BrowserTest
import io.forestframework.testfixtures.RedisSetUpExtension
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.junit.jupiter.api.extension.ExtendWith

abstract class AbstractTodoApplicationBrowserTest extends AbstractBrowserTest {
    @BrowserTest
    void 'run js spec successfully'(Configuration configuration) {
        Browser.drive(configuration) {
            baseUrl = "http://localhost:${port}/js-spec/"
            go "index.html"

            assert title == 'Reference Specs for Todo-Backend'

            $("#target-root-url").value "http://localhost:$port/todos"

            $("#run-tests-btn").click()

            assert $("#target-info > h2").text() == "these tests are targeting: http://localhost:$port/todos"

            // style.css
            assert $('#target-info').css('background-color') in ['rgb(36, 110, 190)', 'rgba(36, 110, 190, 1)']

            waitFor {
                $('#mocha-stats').text().contains('passes: 16failures: 0')
            }

            assert $('.test.pass').size() == 16
        }
    }
}


@ExtendWith(ForestExtension.class)
@ForestIntegrationTest(appClass = TodoApplicationKotlinCoroutinesJDBC.class)
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:TodoApplicationKotlinCoroutinesJDBCBrowserTest;DATABASE_TO_UPPER=false"])
@WithExtensions(extensions = JsSpecExtension.class)
class TodoApplicationKotlinCoroutinesJDBCBrowserTest extends AbstractTodoApplicationBrowserTest {
}


@ExtendWith(EmbeddedRedisExtension.class)
@ExtendWith(RedisSetUpExtension.class)
@ExtendWith(ForestExtension.class)
@ForestIntegrationTest(appClass = TodoApplicationKotlinCoroutinesRedis.class)
@WithExtensions(extensions = JsSpecExtension.class)
class TodoApplicationRedisKotlinCoroutinesBrowserIntegrationTest extends AbstractTodoApplicationBrowserTest {
}

@ExtendWith(ForestExtension.class)
@ForestIntegrationTest(appClass = TodoApplicationJavaAsyncJDBC.class)
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:TodoApplicationJavaAsyncJDBCEndToEndIntegrationTest;DATABASE_TO_UPPER=false"])
@WithExtensions(extensions = JsSpecExtension.class)
class TodoApplicationJavaAsyncJDBCBrowserIntegrationTest extends AbstractTodoApplicationBrowserTest {
}

@ExtendWith(EmbeddedRedisExtension.class)
@ExtendWith(RedisSetUpExtension.class)
@ExtendWith(ForestExtension.class)
@ForestIntegrationTest(appClass = TodoApplicationJavaAsyncRedis.class)
@WithExtensions(extensions = JsSpecExtension.class)
class TodoApplicationJavaAsyncRedisBrowserIntegrationTest extends AbstractTodoApplicationBrowserTest {
}

@ExtendWith(ForestExtension.class)
@ForestIntegrationTest(appClass = TodoApplicationJavaSyncJDBC.class)
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:TodoApplicationJavaSyncJDBCEndToEndIntegrationTest;DATABASE_TO_UPPER=false"])
@WithExtensions(extensions = JsSpecExtension.class)
class TodoApplicationJavaSyncJDBCBrowserIntegrationTest extends AbstractTodoApplicationBrowserTest {
}

@ExtendWith(EmbeddedRedisExtension.class)
@ExtendWith(RedisSetUpExtension.class)
@ExtendWith(ForestExtension.class)
@ForestIntegrationTest(appClass = TodoApplicationJavaSyncRedis.class)
@WithExtensions(extensions = JsSpecExtension.class)
class TodoApplicationJavaSyncBrowserIntegrationTest extends AbstractTodoApplicationBrowserTest {
}

class JsSpecExtension implements Extension {
    @Override
    void start(ApplicationContext context) {
        context.getComponents().add(JsSpecRouter.class)
    }
}


@Router
class JsSpecRouter {
    @GetStaticResource("/js-spec/index.html")
    String index() {
        return "js-spec/index.html"
    }

    @GetStaticResource("/js-spec/js/**")
    String js(@PathParam("**") String path) {
        return "js-spec/js/$path"
    }

    @GetStaticResource("/js-spec/css/**")
    String css(@PathParam("**") String path) {
        return "js-spec/css/$path"
    }
}
