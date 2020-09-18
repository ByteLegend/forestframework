package io.forestframework.core.http

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.websocket.OnWSClose
import io.forestframework.core.http.websocket.OnWSError
import io.forestframework.core.http.websocket.OnWSMessage
import io.forestframework.core.http.websocket.OnWSOpen
import io.forestframework.core.http.websocket.WebSocket
import io.forestframework.core.http.websocket.WebSocketEventType
import io.forestframework.ext.core.IncludeComponents
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.kotlin.core.http.writePingAwait
import io.vertx.kotlin.core.http.writeTextMessageAwait
import io.vertx.kotlin.coroutines.dispatcher
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.lang3.exception.ExceptionUtils
import org.hamcrest.MatcherAssert
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.condition.EnabledOnJre
import org.junit.jupiter.api.condition.JRE.JAVA_11
import org.junit.jupiter.api.extension.ExtendWith

@ForestApplication
class WebSocketTestApp {
    val messages = mutableListOf<String>()

    @WebSocket("/ws1")
    suspend fun webSocketWriteBackDirectly(socket: ServerWebSocket, eventType: WebSocketEventType, message: Buffer) {
        // delay so that client can set up message handler
        delay(1000)

        val messageText = when (eventType) {
            WebSocketEventType.OPEN -> "open"
            WebSocketEventType.MESSAGE -> message.toString().toInt().inc().toString()
            WebSocketEventType.ERROR -> "error"
            WebSocketEventType.CLOSE -> "close"
        }
        messages.add(messageText)
        if (eventType != WebSocketEventType.CLOSE) {
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
                        if (!ExceptionUtils.getStackTrace(t)!!.contains("WebSocket is closed")) {
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
        Assertions.assertNotNull(socket)
        sessions[username] = socket
        broadcast("User $username joined")
    }

    @OnWSClose
    suspend fun onClose(socket: ServerWebSocket, @PathParam("username") username: String) {
        Assertions.assertNotNull(socket)
        sessions.remove(username)
        broadcast("User $username left")
    }

    @OnWSError
    suspend fun onError(socket: ServerWebSocket, @PathParam("username") username: String, throwable: Throwable) {
        Assertions.assertNotNull(socket)
        sessions.remove(username)
        broadcast("User $username left on error")
        errors.add(throwable)
    }

    @OnWSMessage
    suspend fun onMessage(message: String, @PathParam("username") username: String) {
        broadcast(">> $username: $message")
    }

    private suspend fun broadcast(message: String) {
        // Delay so the client can set up message handler
        delay(1000)
        sessions.values.forEach {
            try {
                it.writeTextMessageAwait(message)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}

@ExtendWith(ForestExtension::class)
@ForestTest(appClass = WebSocketTestApp::class)
@DisableAutoScan
@IncludeComponents(classes = [WebSocketChatRoomRouter::class])
@Timeout(120)
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
        delay(5000)

        val expectedServerMessages = expectedClientMessages + listOf("close")
        assertEquals(expectedServerMessages, app.messages)
    }

    @Test
    fun `websocket chat room test`() = runBlockingUnit {
        val alice = openWebsocket("/chat/Alice")

        alice.waitFor("User Alice joined", timeoutMillis = 10000)

        val bob = openWebsocket("/chat/Bob")

        listOf(alice, bob).forEach { it.waitFor("User Bob joined", timeoutMillis = 10000) }

        alice.sendMessage("Hi I'm Alice")
        listOf(alice, bob).forEach { it.waitFor(">> Alice: Hi I'm Alice", timeoutMillis = 10000) }

        val charlie = openWebsocket("/chat/Charlie")
        listOf(alice, bob, charlie).forEach { it.waitFor("User Charlie joined", timeoutMillis = 10000) }
        charlie.sendMessage("Hello from Charlie")
        listOf(alice, bob, charlie).forEach { it.waitFor(">> Charlie: Hello from Charlie", timeoutMillis = 10000) }

        bob.close()

        listOf(alice, charlie).forEach { it.waitFor("User Bob left", timeoutMillis = 10000) }
    }

    @Test
    @EnabledOnJre(value = [JAVA_11])
    fun `chat room error handling test`() = runBlockingUnit {
        val alice = openWebsocket("/chat/Alice")

        val wsClientJavaFile = Paths.get(javaClass.getResource("/WebSocketIntegrationTestData/WebSocketIntegrationTestClient.java").toURI()).toFile()
        val process = ProcessBuilder()
            .inheritIO()
            .command(System.getProperty("java.home") + "/bin/java", "-Dserver.port=$port", "-Duser.name=Bob", wsClientJavaFile.absolutePath)
            .start()

        alice.waitFor("User Bob joined", ">> Bob: Hello from Bob", timeoutMillis = 10000)

        process.destroy()

        delay(1000)

        alice.waitFor("User Bob left on error")

        assertEquals(1, router.errors.size)
    }
}
