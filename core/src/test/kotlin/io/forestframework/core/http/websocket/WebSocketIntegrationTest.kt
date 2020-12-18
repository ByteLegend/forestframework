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
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnJre
import org.junit.jupiter.api.condition.JRE.JAVA_11
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@ForestApplication
class WebSocketTestApp

@Router("/chat/:username")
class SockJSRouter {
    val sessions = ConcurrentHashMap<String, SockJSSocket>()

    @OnWSOpen
    fun onOpen(socket: SockJSSocket, @PathParam("username") username: String) {
        sessions[username] = socket
        broadcast("User $username joined")
    }

    @OnWSClose
    fun onClose(@PathParam("username") username: String) {
        sessions.remove(username)
        broadcast("User $username left")
    }

    @OnWSError
    fun onError(@PathParam("username") username: String, throwable: Throwable) {
        sessions.remove(username)
        broadcast("User $username left on error: $throwable")
    }

    @OnWSMessage
    fun onMessage(message: String, @PathParam("username") username: String) {
        broadcast(">> $username: $message")
    }

    private fun broadcast(message: String) {
        sessions.values.forEach { it.write(message) }
    }
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = WebSocketTestApp::class)
@DisableAutoScan
@IncludeComponents(classes = [SockJSRouter::class])
@Disabled
class SockJSIntegrationTest : AbstractForestIntegrationTest() {
    @Inject
    lateinit var app: WebSocketTestApp

    @Test
    fun `can handle websocket`() = runBlockingUnit {
        val numbers = List(10) { "$it" }
        val expectedNumbers = List(10) { "${it + 1}" }
        openWebsocket("/ws1")
            .apply {
                numbers.forEach { sendMessage(it) }
            }.waitFor(expectedNumbers)
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
            .command(System.getProperty("java.home") + "/bin/java", "-Dserver.port=$port", "-Duser.name=Bob", wsClientJavaFile.absolutePath)
            .start()

        alice.waitFor("User Bob joined", ">> Bob: Hello from Bob")

        process.destroy()

        alice.waitFor("User Bob left on error")
    }
}
