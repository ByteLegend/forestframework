@file:Suppress("UNUSED_PARAMETER")

package io.forestframework.core.http

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.websocket.OnWSClose
import io.forestframework.core.http.websocket.OnWSMessage
import io.forestframework.core.http.websocket.OnWSOpen
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestIntegrationTest
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.inject.Inject

/**
 * The intention of this test to make sure:
 * 1. All `OnWSMessage` methods must be called after `OnWSOpen`.
 * 2. `OnWSClose` must be called after all `OnWSMessage`.
 */
@ForestIntegrationTest(appClass = WebsocketEventSequenceTestApp::class)
@DisableAutoScan
class WebsocketEventSequenceTest : AbstractForestIntegrationTest() {
    @Inject
    lateinit var app: WebsocketEventSequenceTestApp

    @Test
    fun `all OnMessage methods must be invoked after OnOpen and before OnClose`() = runBlockingUnit {
        val client = openWebsocket("/ws")
        for (i in 0 until 10) {
            client.sendMessage(i.toString())
        }
        client.close()

        delay(3000)

        assertEquals(12, app.messages.size)
        assertEquals("open", app.messages.first())
        assertEquals("close", app.messages.last())
    }
}

@ForestApplication
class WebsocketEventSequenceTestApp {
    val messages = mutableListOf<String>()

    @OnWSOpen("/ws")
    suspend fun onOpen() {
        delay(1000)
        messages.add("open")
    }

    @OnWSMessage("/ws")
    suspend fun onMessage(message: String) {
        delay(100)
        messages.add(message)
    }

    @OnWSClose("/ws")
    suspend fun onClose() {
        messages.add("close")
    }
}
