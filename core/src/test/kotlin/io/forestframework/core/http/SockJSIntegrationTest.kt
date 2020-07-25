package io.forestframework.core.http

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.socketjs.SocketJS
import io.forestframework.core.http.socketjs.SocketJSEventType
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ForestApplication
class SockJSTestApp {
    @SocketJS("/ws1")
    fun webSocketWriteBackDirectly(socketJSSocket: SockJSSocket, messageType: SocketJSEventType, message: Buffer) {
    }

    @SocketJS("/ws2")
    fun webSocketWriteBackReturnBuffer(socketJSSocket: SockJSSocket, messageType: SocketJSEventType, message: Buffer): Buffer {
        return message
    }
}


@ExtendWith(ForestExtension::class)
@ForestTest(appClass = SockJSTestApp::class)
@DisableAutoScan
@Disabled
class SockJSIntegrationiTest : AbstractForestIntegrationTest() {
    @Test
    fun `can handle websocket`() {

    }
}