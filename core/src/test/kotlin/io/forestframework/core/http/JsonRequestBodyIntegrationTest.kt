@file:Suppress("UNUSED_PARAMETER")

package io.forestframework.core.http

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.param.JsonRequestBody
import io.forestframework.core.http.result.PlainText
import io.forestframework.core.http.routing.Post
import io.forestframework.core.http.routing.PreHandler
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestIntegrationTest
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test

@ForestApplication
class ReadBodyMultipleTimesIntegrationTestApp {
    @PreHandler("/readMultipleTimes")
    fun readCookie(
        @JsonRequestBody body: String
    ) {
    }

    @Post("/readMultipleTimes")
    @PlainText
    fun getText(@JsonRequestBody body: String) = body

    @PreHandler("/delayInPreHandler")
    suspend fun delayInPreHandler() {
        delay(1000)
    }

    @Post("/delayInPreHandler")
    @PlainText
    fun delayInPreHandler(
        @JsonRequestBody body: String
    ) = body
}

@ForestIntegrationTest(appClass = ReadBodyMultipleTimesIntegrationTestApp::class)
@DisableAutoScan
class ReadBodyMultipleTimesIntegrationTest : AbstractForestIntegrationTest() {
    @Test
    fun canReadRequestMultipleTimes() {
        post("/readMultipleTimes",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{}")
            .assertBody("{}")
    }

    /*
    java.lang.IllegalStateException: Request has already been read
	at io.vertx.core.http.impl.Http1xServerRequest.checkEnded(Http1xServerRequest.java:628) ~[vertx-core-4.0.3.jar:4.0.3]
	at io.vertx.core.http.impl.Http1xServerRequest.body(Http1xServerRequest.java:506) ~[vertx-core-4.0.3.jar:4.0.3]
	at io.forestframework.core.http.DefaultHttpRequest.body(DefaultHttpRequest.java:203)
     */
    @Test
    fun canHaveDelayInPreHandler() {
        post("/delayInPreHandler",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{}")
            .assertBody("{}")
    }
}
