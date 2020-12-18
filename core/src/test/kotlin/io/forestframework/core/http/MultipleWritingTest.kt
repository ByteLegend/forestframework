package io.forestframework.core.http

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.param.QueryParam
import io.forestframework.core.http.result.ResultProcessor
import io.forestframework.core.http.result.RoutingResultProcessor
import io.forestframework.core.http.routing.Get
import io.forestframework.core.http.routing.Routing
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@Retention(AnnotationRetention.RUNTIME)
@Extends(ResultProcessor::class)
@ResultProcessor(by = ReturnProcessor1::class)
annotation class ReturnProcessorAnno1

class ReturnProcessor1 : RoutingResultProcessor {
    override fun processResponse(context: HttpContext, routing: Routing, returnValue: Any?): Any? {
        (context.response().setStatusCode(404) as HttpResponse).writeLater("BBB")
        return returnValue
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Extends(ResultProcessor::class)
@ResultProcessor(by = ReturnProcessor2::class)
annotation class ReturnProcessorAnno2

class ReturnProcessor2 : RoutingResultProcessor {
    override fun processResponse(context: HttpContext, routing: Routing, returnValue: Any?): Any? {
        (context.response().setStatusCode(500) as HttpResponse).writeLater("CCC")
        return returnValue
    }
}

@ForestApplication
class MultiWritingTestApp {
    @Get("/multiwriting")
    @ReturnProcessorAnno1
    @ReturnProcessorAnno2
    fun endpoint(response: HttpResponse) {
        response.setStatusCode(201).writeLater("A").writeLater("A").writeLater(Buffer.buffer("A"))
    }

    @Get("/writerLaterThenWriteThenWriteLater")
    fun writerLaterThenWriteThenWriteLater(response: HttpResponse, @QueryParam("chunked") chunked: Boolean): Future<Any> {
        response.isChunked = chunked
        return response.writeLater("AAA")
            .write("BBB").map {
                response.writeLater("CCC")
            }
    }
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = MultiWritingTestApp::class)
@DisableAutoScan
class MultipleWritingTest : AbstractForestIntegrationTest() {
    @Test
    fun `can write multiple times`() = runBlockingUnit {
        get("/multiwriting").assert500().assertBody("AAABBBCCC")
    }

    @Test
    fun `can writeLater then write then writeLater`() = runBlockingUnit {
        get("/writerLaterThenWriteThenWriteLater?chunked=true").assert200().assertBody("AAABBBCCC")
    }

    @Test
    fun `throw exception if write without chunked encoding`() = runBlockingUnit {
        get("/writerLaterThenWriteThenWriteLater?chunked=false").assert500()
    }
}
