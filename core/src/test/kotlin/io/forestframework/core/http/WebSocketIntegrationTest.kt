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
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.kotlin.core.http.writeTextMessageAwait
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

    @WebSocket("/ws1")
    suspend fun webSocketWriteBackDirectly(socket: ServerWebSocket, eventType: WebSocketEventType, message: Buffer) {
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
class WebSocketChatRoomRouter {
    val sessions = ConcurrentHashMap<String, ServerWebSocket>()

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
        broadcast("User $username left on error: $throwable")
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

    @Test
    fun `can handle websocket`() = runBlockingUnit {
        val numbers = List(10) { "$it" }
        val expectedClientMessages = listOf("open") + List(10) { "${it + 1}" }
        openWebsocket("/ws1")
            .apply {
                numbers.forEach { sendMessage(it) }
            }.waitFor(expectedClientMessages)
            .close()

        val expectedServerMessages = expectedClientMessages + listOf("close")
        assertEquals(expectedServerMessages, app.messages)
    }

    @Test
    fun `websocket chat room test`() = runBlockingUnit {
        val alice = openWebsocket("/chat/Alice")

        alice.waitFor("User Alice joined")

        val bob = openWebsocket("/chat/Bob")

        listOf(alice, bob).forEach { it.waitFor("User Bob joined") }

        alice.sendMessage("Hi I'm Alice")
        listOf(alice, bob).forEach { it.waitFor(">> Alice: Hi I'm Alice") }

        val charlie = openWebsocket("/chat/Charlie")
        listOf(alice, bob, charlie).forEach { it.waitFor("User Charlie joined") }
        charlie.sendMessage("Hello from Charlie")
        listOf(alice, bob, charlie).forEach { it.waitFor(">> Charlie: Hello from Charlie") }

        bob.close()
        listOf(alice, charlie).forEach { it.waitFor("User Bob left") }
    }

    @Test
    @EnabledOnJre(value = [JAVA_11])
    fun `chat room error handling test`() = runBlockingUnit {
        val alice = openWebsocket("/chat/Alice")

        val wsClientJavaFile = Paths.get(javaClass.getResource("/SockJSIntegrationTestData/SocketJSIntegrationTestClient.java").toURI()).toFile()
        val process = ProcessBuilder()
            .inheritIO()
            .command(System.getProperty("java.home") + "/bin/java", "-Dserver.port=${port}", "-Duser.name=Bob", wsClientJavaFile.absolutePath)
            .start()

        alice.waitFor("User Bob joined", ">> Bob: Hello from Bob")

        process.destroy()

        alice.waitFor("User Bob left on error")
    }
}