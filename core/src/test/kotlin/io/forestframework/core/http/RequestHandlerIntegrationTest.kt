package io.forestframework.core.http

import com.google.inject.Injector
import io.forestframework.core.ForestApplication
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import javax.inject.Inject

@ForestApplication
class TestApp

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = TestApp::class)
@DisableAutoScan
class RequestHandlerIntegrationTest : AbstractForestIntegrationTest() {
    @Inject
    lateinit var injector: Injector

    @Test
    fun `RequestHandler is singleton`() {
        Assertions.assertSame(injector.getInstance(HttpRequestHandler::class.java), injector.getInstance(HttpRequestHandler::class.java))
    }
}
