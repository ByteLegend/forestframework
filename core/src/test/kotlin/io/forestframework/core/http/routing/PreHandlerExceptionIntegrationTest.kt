package io.forestframework.core.http.routing

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.HttpResponse
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import kotlinx.coroutines.delay
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ForestApplication
class PreHandlerExceptionIntegrationTestApp {
    @PreHandler("/preSyncError")
    fun preSyncError() {
        throw IllegalStateException("preSyncError")
    }

    @Get("/*")
    fun main() {
    }

    @PreHandler("/preAsyncError")
    fun preAsyncError(vertx: Vertx): Future<Void> {
        val promise = Promise.promise<Void>()
        vertx.setTimer(100) {
            promise.fail(IllegalStateException("preAsyncError"))
        }
        return promise.future()
    }

    @PreHandler("/preSuspendError")
    suspend fun preSuspendError() {
        delay(100)
        throw IllegalStateException("preSuspendError")
    }

    @OnError("/*")
    fun handleError(throwable: Throwable, response: HttpResponse) {
        response.writeLater(throwable.message)
    }
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = PreHandlerExceptionIntegrationTestApp::class)
@DisableAutoScan
class PreHandlerExceptionIntegrationTest : AbstractForestIntegrationTest() {
    @ParameterizedTest
    @ValueSource(strings = ["preSyncError", "preAsyncError", "preSuspendError"])
    fun test(path: String) = runBlockingUnit {
        get("/$path").assert500().assertBody("java.lang.IllegalStateException: $path")
    }
}
