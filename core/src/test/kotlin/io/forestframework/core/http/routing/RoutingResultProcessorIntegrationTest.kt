package io.forestframework.core.http.routing

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.HttpContext
import io.forestframework.core.http.HttpResponse
import io.forestframework.core.http.result.ResultProcessor
import io.forestframework.core.http.result.RoutingResultProcessor
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@Retention(AnnotationRetention.RUNTIME)
@Extends(ResultProcessor::class)
@ResultProcessor(by = NoOpResultProcessor::class)
annotation class NoOp

class NoOpResultProcessor : RoutingResultProcessor {
    override fun processResponse(context: HttpContext, routing: Routing, returnValue: Any?) = returnValue
}

@ForestApplication
class RoutingResultProcessorIntegrationTestApp {
    @Get("/noop")
    @NoOp
    fun noop() {
    }

    @Get("/noopOnlyWrite")
    @NoOp
    fun noopOnlyWrite(response: HttpResponse) {
        response.writeLater("noopOnlyWrite")
    }
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = RoutingResultProcessorIntegrationTestApp::class)
@DisableAutoScan
class RoutingResultProcessorIntegrationTest : AbstractForestIntegrationTest() {
    @Test
    fun noopTest() = runBlockingUnit {
        get("/noop").assert200().assertBody("")
    }

    @Test
    fun noopOnlyWriteTest() = runBlockingUnit {
        get("/noopOnlyWrite").assert200().assertBody("noopOnlyWrite")
    }
}
