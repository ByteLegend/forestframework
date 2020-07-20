package io.forestframework.example

import geb.Browser
import geb.Page
import io.forestframework.core.SingletonRouter
import io.forestframework.core.config.Config
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.staticresource.GetStaticResource
import io.forestframework.example.todo.kotlin.TodoApplicationJDBC
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.StartupContext
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import javax.inject.Inject
import java.util.concurrent.CountDownLatch


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


class TodoApplicationRedisKotlinEndToEndTest {
}

@ExtendWith(ForestExtension.class)
@ForestTest(appClass = TodoApplicationJDBC.class,
        extraConfigs = ["forest.jdbc.url=jdbc:h2:mem:todo;DATABASE_TO_UPPER=false"],
        extraExtensions = JsSpecExtension.class)
class TodoApplicationJDBCKotlinEndToEndTest extends AbstractTodoApplicationEndToEndTest {
}


class JsSpecExtension implements Extension {
    @Override
    void beforeInjector(StartupContext context) {
        context.getComponentClasses().add(JsSpecRouter.class)
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