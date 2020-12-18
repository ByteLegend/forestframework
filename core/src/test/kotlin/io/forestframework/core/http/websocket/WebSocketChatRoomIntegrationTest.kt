package io.forestframework.core.http.websocket

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.Router
import io.forestframework.core.http.param.PathParam
import io.forestframework.ext.core.IncludeComponents
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import io.vertx.core.http.ServerWebSocket
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.condition.EnabledOnJre
import org.junit.jupiter.api.condition.JRE.JAVA_11
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@ForestApplication
class WebSocketChatRoomApp

@Router("/chat/:username")
class WebSocketChatRoomRouter {
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
        try {
            Assertions.assertNotNull(socket)
            if (sessions.remove(username) != null) {
                // Sometimes (e.g. on Windows), when client disconnects, two exceptions are triggered:
                // 1. io.vertx.core.VertxException: Connection was closed
                // 2. java.io.IOException: An existing connection was forcibly closed by the remote host
                broadcast("User $username left on error")
                errors.add(throwable)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @OnWSMessage
    suspend fun onMessage(message: String, @PathParam("username") username: String) {
        broadcast(">> $username: $message")
    }

    private suspend fun broadcast(message: String) {
        // Delay so the client can set up message handler
        delay(1000)
        sessions.entries.forEach {
            try {
                it.value.writeTextMessage(message).await()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = WebSocketChatRoomApp::class)
@DisableAutoScan
@IncludeComponents(classes = [WebSocketChatRoomRouter::class])
@Timeout(120)
class WebSocketChatRoomIntegrationTest : AbstractForestIntegrationTest() {

    @Inject
    lateinit var router: WebSocketChatRoomRouter
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

        delay(2000)
        alice.waitFor("User Bob joined", ">> Bob: Hello from Bob", timeoutMillis = 10000)

        process.destroy()

        delay(2000)

        alice.waitFor("User Bob left on error")

        assertEquals(1, router.errors.size)
    }
}
