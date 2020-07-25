package io.forestframework.example

import geb.Browser
import io.forestframework.core.SingletonRouter
import io.forestframework.core.config.Config
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.staticresource.GetStaticResource
import io.forestframework.example.todo.java.async.jdbc.TodoApplicationJavaAsyncJDBC
import io.forestframework.example.todo.java.async.redis.TodoApplicationJavaAsyncRedis
import io.forestframework.example.todo.java.sync.redis.TodoApplicationJavaSyncRedis
import io.forestframework.example.todo.kotlin.jdbc.TodoApplicationKotlinCoroutinesJDBC
import io.forestframework.example.todo.kotlin.redis.TodoApplicationKotlinCoroutinesRedis
import io.forestframework.ext.api.EnableExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.StartupContext
import io.forestframework.ext.core.ExtraConfig
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import se.svt.util.junit5.redis.EmbeddedRedisExtension
import se.svt.util.junit5.redis.EmbeddedRedisExtensionKt

import javax.inject.Inject

abstract class AbstractTodoApplicationEndToEndTest {
    @Inject
    @Config("forest.http.port")
    int port

    @BeforeAll
    static void setUp() {
        System.setProperty("webdriver.gecko.driver", "/Users/zhb/Develop/geckodriver/geckodriver")
    }

    @Test
    void 'run js spec successfully'() {
        Browser.drive {
            baseUrl = "http://localhost:${port}/js-spec/"
            go "index.html"

            assert title == 'Reference Specs for Todo-Backend'

            $("#target-root-url").value "http://localhost:$port/todos"

            $("#run-tests-btn").click()

            assert $("#target-info > h2").text() == "these tests are targeting: http://localhost:$port/todos"

            // style.css
            assert $('#target-info').css('background-color') == 'rgb(36, 110, 190)'

            waitFor {
                $('#mocha-stats').text().contains('passes: 16failures: 0')
            }

            assert $('.test.pass').size() == 16
        }
    }
}


@ExtendWith(ForestExtension.class)
@ForestTest(appClass = TodoApplicationKotlinCoroutinesJDBC.class)
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:todo;DATABASE_TO_UPPER=false"])
@EnableExtensions(extensions = JsSpecExtension.class)
class TodoApplicationKotlinCoroutinesJDBCEndToEndTest extends AbstractTodoApplicationEndToEndTest {
}

@ExtendWith(EmbeddedRedisExtension.class)
@ExtendWith(RedisSetUpExtension.class)
@ExtendWith(ForestExtension.class)
@ForestTest(appClass = TodoApplicationKotlinCoroutinesRedis.class)
@EnableExtensions(extensions = JsSpecExtension.class)
class TodoApplicationRedisKotlinCoroutinesEndToEndIntegrationTest extends AbstractTodoApplicationEndToEndTest {
}

@ExtendWith(ForestExtension.class)
@ForestTest(appClass = TodoApplicationJavaAsyncJDBC.class)
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:todo;DATABASE_TO_UPPER=false"])
@EnableExtensions(extensions = JsSpecExtension.class)
class TodoApplicationJavaAsyncJDBCEndToEndIntegrationTest extends AbstractTodoApplicationEndToEndTest {
}

@ExtendWith(EmbeddedRedisExtension.class)
@ExtendWith(RedisSetUpExtension.class)
@ExtendWith(ForestExtension.class)
@ForestTest(appClass = TodoApplicationJavaAsyncRedis.class)
@EnableExtensions(extensions = JsSpecExtension.class)
class TodoApplicationJavaAsyncRedisEndToEndIntegrationTest extends AbstractTodoApplicationEndToEndTest {
}

@ExtendWith(EmbeddedRedisExtension.class)
@ExtendWith(RedisSetUpExtension.class)
@ExtendWith(ForestExtension.class)
@ForestTest(appClass = TodoApplicationJavaSyncRedis.class)
@EnableExtensions(extensions = JsSpecExtension.class)
class TodoApplicationJavaSyncEndToEndIntegrationTest extends AbstractTodoApplicationEndToEndTest {
}

class JsSpecExtension implements Extension {
    @Override
    void beforeInjector(StartupContext context) {
        context.getComponentClasses().add(JsSpecRouter.class)
    }
}

class RedisSetUpExtension implements BeforeAllCallback {
    @Override
    void beforeAll(ExtensionContext context) {
        System.setProperty("forest.redis.endpoints", "[\"redis://localhost:${System.getProperty(EmbeddedRedisExtensionKt.REDIS_PORT_PROPERTY)}\"]")
    }
}


@SingletonRouter
class JsSpecRouter {
    @GetStaticResource("/js-spec/index.html")
    String index() {
        return "js-spec/index.html"
    }

    @GetStaticResource("/js-spec/js/*")
    String js(@PathParam("*") String path) {
        return "js-spec/js/$path"
    }

    @GetStaticResource("/js-spec/css/*")
    String css(@PathParam("*") String path) {
        return "js-spec/css/$path"
    }
}