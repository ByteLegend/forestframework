package io.forestframework.core.websocket

import geb.Browser
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.Blocking
import io.forestframework.core.http.Router
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.websocket.OnWSClose
import io.forestframework.core.http.websocket.OnWSError
import io.forestframework.core.http.websocket.OnWSMessage
import io.forestframework.core.http.websocket.OnWSOpen
import io.forestframework.ext.core.AutoStaticResourceScan
import io.forestframework.testfixtures.AbstractBrowserTest
import io.forestframework.testfixtures.BrowserTest
import io.forestframework.testsupport.ForestIntegrationTest
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import org.apache.commons.lang3.exception.ExceptionUtils

import javax.inject.Inject
import java.util.concurrent.ConcurrentHashMap

@AutoStaticResourceScan(webroot = "WebSocketChatRoomBrowserTestData")
@ForestApplication
class WebSocketChatRoomBrowserTestApp {
    static void main(String[] args) {
        Forest.run(WebSocketChatRoomBrowserTestApp.class)
    }
}

@Router("/chat/:username")
@Blocking
class WebSocketChatRoomRouter {
    private ConcurrentHashMap<String, ServerWebSocket> sessions = new ConcurrentHashMap<String, ServerWebSocket>()
    private List<Throwable> errors = []

    @Inject
    WebSocketChatRoomRouter(Vertx vertx) {
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

@ForestIntegrationTest(appClass = WebSocketChatRoomBrowserTestApp.class)
class WebSocketChatRoomBrowserTest extends AbstractBrowserTest {
    @BrowserTest
    void chatroomTest(Map conf) {
        Browser.drive(conf) {
            baseUrl = "http://$host:${port}/"
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

