@file:Suppress("UNUSED_PARAMETER")

package io.forestframework.core.http

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.param.Header
import io.forestframework.core.http.websocket.OnWSOpen
import io.forestframework.core.http.websocket.WebSocket
import io.forestframework.core.http.websocket.WebSocketEventType
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.ServerWebSocket
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.delay
import org.hamcrest.MatcherAssert
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import javax.inject.Inject

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
            socket.writeTextMessage(messageText).await()
        }
    }

    @OnWSOpen("/headerTest")
    suspend fun getHeader(socket: ServerWebSocket, @Header("testHeader") header: String) {
        messages.add(header)
    }
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = WebSocketTestApp::class)
@DisableAutoScan
@Timeout(120)
class WebSocketIntegrationTest : AbstractForestIntegrationTest() {
    @Inject
    lateinit var app: WebSocketTestApp

    @BeforeEach
    fun setUp() {
        app.messages.clear()
    }

    @Test
    fun `can parse header in ws`() = runBlockingUnit {
        openWebsocketAbs("ws://localhost:$port/headerTest", mapOf("testHeader" to "abc"))

        delay(1000)
        assertEquals(listOf("abc"), app.messages)
    }

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
}
