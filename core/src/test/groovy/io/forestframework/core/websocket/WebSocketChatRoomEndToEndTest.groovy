package io.forestframework.core.websocket

import geb.Browser
import geb.Configuration
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.core.config.Config
import io.forestframework.core.http.Blocking
import io.forestframework.core.http.Router
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.websocket.OnWSClose
import io.forestframework.core.http.websocket.OnWSError
import io.forestframework.core.http.websocket.OnWSMessage
import io.forestframework.core.http.websocket.OnWSOpen
import io.forestframework.ext.core.WithStaticResource
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import org.apache.commons.lang3.exception.ExceptionUtils
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

import javax.inject.Inject
import java.util.concurrent.ConcurrentHashMap

@ForestApplication
@WithStaticResource(webroot = "WebSocketChatRoomEndToEndTestData")
class WebSocketChatRoomEndToEndTestApp {
    static void main(String[] args) {
        Forest.run(WebSocketChatRoomEndToEndTestApp.class)
    }
}

@Router("/chat/:username")
@Blocking
class WebSocketChatRoomRouter {
    private ConcurrentHashMap<String, ServerWebSocket> sessions = new ConcurrentHashMap<String, ServerWebSocket>()
    private List<Throwable> errors = []

    @Inject
    WebSocketChatRoomEndToEndTestApp(Vertx vertx) {
        vertx.setPeriodic(100) {
            sessions.values().forEach {
                try {
                    it.writePing(Buffer.buffer("ping"))
                } catch (Throwable t) {
                    if (!ExceptionUtils.getStackTrace(t)?.contains("WebSocket is closed")) {
                        throw t
                    }
                }
            }
        }
    }

    @OnWSOpen
    void onOpen(ServerWebSocket socket, @PathParam("username") String username) {
        sessions[username] = socket
        broadcast("User $username joined")
    }

    @OnWSClose
    void onClose(ServerWebSocket socket, @PathParam("username") String username) {
        sessions.remove(username)
        broadcast("User $username left")
    }

    @OnWSError
    void onError(ServerWebSocket socket, @PathParam("username") String username, Throwable throwable) {
        sessions.remove(username)
        broadcast("User $username left on error")
        errors.add(throwable)
    }

    @OnWSMessage
    void onMessage(String message, @PathParam("username") String username) {
        broadcast(">> $username: $message")
    }

    private void broadcast(String message) {
        assert !Thread.currentThread().name.contains("vert.x-eventloop"): "Blocking handler should not be running inside eventloop thread, but was: ${Thread.currentThread().name}"
        sessions.values().each {
            it.writeTextMessage(message)
        }
    }
}


@ExtendWith(ForestExtension.class)
@ForestTest(appClass = WebSocketChatRoomEndToEndTestApp.class)
class WebSocketChatRoomEndToEndTest {
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
    void 'websocket chat room test'(String envName, String systemPropertyName, String driver) {
        Assumptions.assumeFalse(System.getenv(envName) == null)

        System.setProperty(systemPropertyName, System.getenv(envName))

        Browser.drive(new Configuration(driver: driver)) {
            baseUrl = "http://localhost:${port}/"
            go "/index.html"

            assert title == 'Quarkus Chat!'

            $("#name").value "Alice"

            $("#connect").click()

            waitFor {
                $("#connect").attr("disabled") == 'true'
            }

            waitFor {
                $("#chat").value().toString().contains("User Alice joined")
            }

            $('#msg').value("Hi!")
            $('#send').click()

            waitFor {
                $("#chat").value().toString().contains(">> Alice: Hi!")
            }
        }
    }
}

