@file:Suppress("UNUSED_PARAMETER")

package io.forestframework.core.http

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.param.Cookie
import io.forestframework.core.http.param.JsonRequestBody
import io.forestframework.core.http.result.PlainText
import io.forestframework.core.http.routing.Post
import io.forestframework.core.http.routing.PreHandler
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ForestApplication
class ReadBodyMultipleTimesIntegrationTestApp {
    @PreHandler("/**")
    fun readCookie(@Cookie("JSESSION_ID") cookie: String, @JsonRequestBody body: String) {
    }

    @Post("/post")
    @PlainText
    fun getText(@JsonRequestBody body: String) = body
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = ReadBodyMultipleTimesIntegrationTestApp::class)
@DisableAutoScan
class ReadBodyMultipleTimesIntegrationTest : AbstractForestIntegrationTest() {
    @Test
    fun canReadRequestMultipleTimes() {
        post("/post",
            headers = mapOf("Cookie" to "JSESSION_ID=1", "Content-Type" to "application/json"),
            body = "{}")
            .assertBody("{}")
    }
}
