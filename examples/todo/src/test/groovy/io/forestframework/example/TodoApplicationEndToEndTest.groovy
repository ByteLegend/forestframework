package io.forestframework.example

import geb.Browser
import geb.Configuration
import io.forestframework.core.config.Config
import io.forestframework.core.http.Router
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.staticresource.GetStaticResource
import io.forestframework.example.todo.java.async.jdbc.TodoApplicationJavaAsyncJDBC
import io.forestframework.example.todo.java.async.redis.TodoApplicationJavaAsyncRedis
import io.forestframework.example.todo.java.sync.jdbc.TodoApplicationJavaSyncJDBC
import io.forestframework.example.todo.java.sync.redis.TodoApplicationJavaSyncRedis
import io.forestframework.example.todo.kotlin.jdbc.TodoApplicationKotlinCoroutinesJDBC
import io.forestframework.example.todo.kotlin.redis.TodoApplicationKotlinCoroutinesRedis
import io.forestframework.ext.api.EnableExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.StartupContext
import io.forestframework.ext.core.ExtraConfig
import io.forestframework.testfixtures.EmbeddedRedisExtension
import io.forestframework.testfixtures.RedisSetUpExtension
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

import javax.inject.Inject

abstract class AbstractTodoApplicationEndToEndTest {
    @Inject
    @Config("forest.http.port")
    int port

    @ParameterizedTest(name = "run js spec successfully with {0}")
    @CsvSource([
            //https://github.com/actions/virtual-environments/blob/c647d2d4ef048c2bb9d98df65a3a05686a095d8e/images/linux/scripts/installers/firefox.sh
            "GECKODRIVER_BIN, webdriver.gecko.driver,   org.openqa.selenium.firefox.FirefoxDriver",
            // https://github.com/actions/virtual-environments/blob/970e8f5c4f87515f5c75e569a7eb467d3a9e5ae5/images/linux/scripts/installers/google-chrome.sh#L43
            "CHROMEDRIVER_BIN, webdriver.chrome.driver,  org.openqa.selenium.chrome.ChromeDriver"
    ])
    void 'run js spec successfully'(String envName, String systemPropertyName, String driver) {
        Assumptions.assumeFalse(System.getenv(envName) == null)

        System.setProperty(systemPropertyName, System.getenv(envName))

        Browser.drive(new Configuration(driver: driver)) {
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
@ForestTest(appClass = TodoApplicationKotlinCoroutinesJDBC.class)
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:TodoApplicationKotlinCoroutinesJDBCEndToEndTest;DATABASE_TO_UPPER=false"])
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
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:TodoApplicationJavaAsyncJDBCEndToEndIntegrationTest;DATABASE_TO_UPPER=false"])
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

@ExtendWith(ForestExtension.class)
@ForestTest(appClass = TodoApplicationJavaSyncJDBC.class)
@ExtraConfig(value = ["forest.jdbc.url=jdbc:h2:mem:TodoApplicationJavaSyncJDBCEndToEndIntegrationTest;DATABASE_TO_UPPER=false"])
@EnableExtensions(extensions = JsSpecExtension.class)
class TodoApplicationJavaSyncJDBCEndToEndIntegrationTest extends AbstractTodoApplicationEndToEndTest {
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