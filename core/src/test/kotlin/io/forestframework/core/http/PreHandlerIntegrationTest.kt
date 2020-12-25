package io.forestframework.core.http

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.core.http.routing.PreHandler
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestIntegrationTest
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test

@ForestApplication
class PreHandlerIntegrationTestApp {
    @PreHandler("/**")
    suspend fun preHandlerReturningUnit() {
        delay(100)
    }

    @GetPlainText("/test")
    fun test() = ""
}

@DisableAutoScan
@ForestIntegrationTest(appClass = PreHandlerIntegrationTestApp::class)
class PreHandlerIntegrationTest : AbstractForestIntegrationTest() {
    @Test
    fun test() {
        get("/test").assert200()
    }
}
