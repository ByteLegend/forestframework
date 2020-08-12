package io.forestframework.core.http

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.routing.RoutingType
import io.forestframework.core.http.sockjs.OnClose
import io.forestframework.core.http.websocket.OnWSClose
import io.forestframework.core.http.websocket.OnWSError
import io.forestframework.core.http.websocket.OnWSMessage
import io.forestframework.core.http.websocket.OnWSOpen
import io.forestframework.ext.core.IncludeComponents
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.kotlin.core.http.writePingAwait
import io.vertx.kotlin.core.http.writeTextMessageAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.lang3.exception.ExceptionUtils
import org.hamcrest.MatcherAssert
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnJre
import org.junit.jupiter.api.condition.JRE.JAVA_11
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@ForestApplication
class WebSocketTestApp {
    val messages = mutableListOf<String>()

    @OnWSOpen("/ws1")
    suspend fun onOpen(socket: ServerWebSocket, message: Buffer) = webSocketWriteBackDirectly(socket, RoutingType.ON_WEB_SOCKET_OPEN, message)

    @OnWSClose("/ws1")
    suspend fun onClose(socket: ServerWebSocket, message: Buffer) = webSocketWriteBackDirectly(socket, RoutingType.ON_WEB_SOCKET_CLOSE, message)

    @OnWSMessage("/ws1")
    suspend fun onMessage(socket: ServerWebSocket, message: Buffer) = webSocketWriteBackDirectly(socket, RoutingType.ON_WEB_SOCKET_MESSAGE, message)

    @OnClose("/ws1")
    suspend fun onError(socket: ServerWebSocket, message: Buffer) = webSocketWriteBackDirectly(socket, RoutingType.ON_WEB_SOCKET_ERROR, message)

    private suspend fun webSocketWriteBackDirectly(socket: ServerWebSocket, eventType: RoutingType, message: Buffer) {
        // delay so that client can set up message handler
        delay(500)

        val messageText = when (eventType) {
            RoutingType.ON_WEB_SOCKET_OPEN -> "open"
            RoutingType.ON_WEB_SOCKET_MESSAGE -> message.toString().toInt().inc().toString()
            RoutingType.ON_WEB_SOCKET_ERROR -> "error"
            RoutingType.ON_WEB_SOCKET_CLOSE -> "close"
            else -> throw IllegalStateException()
        }
        messages.add(messageText)
        if (eventType != RoutingType.ON_WEB_SOCKET_CLOSE) {
            socket.writeTextMessageAwait(messageText)
        }
    }
}

@Router("/chat/:username")
class WebSocketChatRoomRouter @Inject constructor(vertx: Vertx) {
    init {
        vertx.setPeriodic(100) {
            sessions.values.forEach {
                GlobalScope.launch(vertx.dispatcher()) {
                    try {
                        it.writePingAwait(Buffer.buffer("ping"))
                    } catch (t: Throwable) {
                        if (!ExceptionUtils.getStackTrace(t)!!.contentEquals("WebSocket is closed")) {
                            throw t
                        }
                    }
                }
            }
        }
    }

    val sessions = ConcurrentHashMap<String, ServerWebSocket>()
    val errors = ArrayList<Throwable>()

    @OnWSOpen
    suspend fun onOpen(socket: ServerWebSocket, @PathParam("username") username: String) {
        sessions[username] = socket
        broadcast("User $username joined")
    }

    @OnWSClose
    suspend fun onClose(socket: ServerWebSocket, @PathParam("username") username: String) {
        sessions.remove(username)
        broadcast("User $username left")
    }

    @OnWSError
    suspend fun onError(socket: ServerWebSocket, @PathParam("username") username: String, throwable: Throwable) {
        sessions.remove(username)
        broadcast("User $username left on error")
        errors.add(throwable)
    }

    @OnWSMessage
    suspend fun onMessage(message: String, @PathParam("username") username: String) {
        broadcast(">> $username: $message")
    }

    private suspend fun broadcast(message: String) {
        sessions.values.forEach { it.writeTextMessageAwait(message) }
    }
}

@ExtendWith(ForestExtension::class)
@ForestTest(appClass = WebSocketTestApp::class)
@DisableAutoScan
@IncludeComponents(classes = [WebSocketChatRoomRouter::class])
class WebSocketIntegrationTest : AbstractForestIntegrationTest() {
    @Inject
    lateinit var app: WebSocketTestApp

    @Inject
    lateinit var router: WebSocketChatRoomRouter

    @Test
    fun `return 404 if not found`() = runBlockingUnit {
        try {
            openWebsocket("/inexistent")
            Assertions.assertTrue(false)
        } catch (e: Exception) {
            MatcherAssert.assertThat(e.message, StringContains.containsString("WebSocket connection attempt returned HTTP status code 404"))
        }
    }

    @Test
    fun `can handle websocket`() = runBlockingUnit {
        val numbers = List(10) { "$it" }
        val expectedClientMessages = listOf("open") + List(10) { "${it + 1}" }
        openWebsocket("/ws1")
            .apply {
                numbers.forEach { sendMessage(it) }
            }.waitFor(expectedClientMessages)
            .close()

        // Wait for server receiving the close event
        delay(500)

        val expectedServerMessages = expectedClientMessages + listOf("close")
        assertEquals(expectedServerMessages, app.messages)
    }

    @Test
    fun `websocket chat room test`() = runBlockingUnit {
        val alice = openWebsocket("/chat/Alice")

        alice.waitFor("User Alice joined")

        val bob = openWebsocket("/chat/Bob")

        listOf(alice, bob).forEach { it.waitFor("User Bob joined", timeoutMillis = 5000) }

        alice.sendMessage("Hi I'm Alice")
        listOf(alice, bob).forEach { it.waitFor(">> Alice: Hi I'm Alice", timeoutMillis = 5000) }

        val charlie = openWebsocket("/chat/Charlie")
        listOf(alice, bob, charlie).forEach { it.waitFor("User Charlie joined", timeoutMillis = 5000) }
        charlie.sendMessage("Hello from Charlie")
        listOf(alice, bob, charlie).forEach { it.waitFor(">> Charlie: Hello from Charlie", timeoutMillis = 5000) }

        bob.close()

        listOf(alice, charlie).forEach { it.waitFor("User Bob left", timeoutMillis = 5000) }
    }

    @Test
    @EnabledOnJre(value = [JAVA_11])
    fun `chat room error handling test`() = runBlockingUnit {
        val alice = openWebsocket("/chat/Alice")

        val wsClientJavaFile = Paths.get(javaClass.getResource("/WebSocketIntegrationTestData/WebSocketIntegrationTestClient.java").toURI()).toFile()
        val process = ProcessBuilder()
            .inheritIO()
            .command(System.getProperty("java.home") + "/bin/java", "-Dserver.port=${port}", "-Duser.name=Bob", wsClientJavaFile.absolutePath)
            .start()

        alice.waitFor("User Bob joined", ">> Bob: Hello from Bob", timeoutMillis = 2000)

        process.destroy()

        delay(1000)

        alice.waitFor("User Bob left on error")

        Assertions.assertEquals(1, router.errors.size)
    }
}