package io.forestframework.core.http.result

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.HttpMethod
import io.forestframework.core.http.param.QueryParam
import io.forestframework.core.http.routing.Get
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ForestApplication
class RedirectResultProcessorIntegrationTestApp {
    @Get("/redirect")
    @Redirect
    fun redirect(@QueryParam("to") to: String) = to
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = RedirectResultProcessorIntegrationTestApp::class)
@DisableAutoScan
class RedirectResultProcessorIntegrationTest : AbstractForestIntegrationTest() {
    @Test
    fun `can redirect`() {
        send(HttpMethod.GET, "/redirect?to=/another")
            .assertStatusCode(302)
            .assertHeader("location", "/another")
    }
}
