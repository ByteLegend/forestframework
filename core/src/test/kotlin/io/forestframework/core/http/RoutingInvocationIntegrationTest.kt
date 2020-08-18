@file:Suppress("UNUSED_PARAMETER")

package io.forestframework.core.http

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.param.ParameterResolver
import io.forestframework.core.http.param.RoutingParameterResolver
import io.forestframework.core.http.result.PlainText
import io.forestframework.core.http.result.ResultProcessor
import io.forestframework.core.http.result.RoutingResultProcessor
import io.forestframework.core.http.routing.Get
import io.forestframework.core.http.routing.On500
import io.forestframework.core.http.routing.Routing
import io.forestframework.ext.core.IncludeComponents
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import io.vertx.core.Promise
import io.vertx.core.Vertx
import javax.inject.Inject
import javax.inject.Singleton
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ForestApplication
class RoutingInvocationIntegrationTestApp

@Singleton
class OrderTestRouter @Inject constructor(val vertx: Vertx) {
    val list = mutableListOf<String>()

    // param1 resolve: 4s
    // param2 resolve: 1s
    // param3 resolve: 3s
    // handler invocation: 2s
    // result processor: 0.5s
    // result processor: 1.5s
    @Get("/orderTest")
    @OneAndHalfSecondResultProcessorAnno
    @HalfSecondResultProcessorAnno
    fun orderTest(
        @FourSecondParamResolverAnno param1: Any,
        @OneSecondParamResolverAnno param2: Any,
        @ThreeSecondParamResolverAnno param3: Any
    ) = delayFuture(vertx, 2000) {
        list.add(OrderTestRouter::class.java.simpleName)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Extends(ParameterResolver::class)
@ParameterResolver(resolver = ThreeSecondParamResolver::class)
annotation class ThreeSecondParamResolverAnno

@Retention(AnnotationRetention.RUNTIME)
@Extends(ParameterResolver::class)
@ParameterResolver(resolver = FourSecondParamResolver::class)
annotation class FourSecondParamResolverAnno

@Retention(AnnotationRetention.RUNTIME)
@Extends(ResultProcessor::class)
@ResultProcessor(by = HalfSecondResultProcessor::class)
annotation class HalfSecondResultProcessorAnno

@Retention(AnnotationRetention.RUNTIME)
@Extends(ResultProcessor::class)
@ResultProcessor(by = OneAndHalfSecondResultProcessor::class)
annotation class OneAndHalfSecondResultProcessorAnno

@Retention(AnnotationRetention.RUNTIME)
@Extends(ParameterResolver::class)
@ParameterResolver(resolver = OneSecondParamResolver::class)
annotation class OneSecondParamResolverAnno

abstract class AbstractDelayedParamResolver constructor(val millis: Long, val vertx: Vertx, val orderTestRouter: OrderTestRouter) : RoutingParameterResolver<Any> {
    override fun resolveParameter(context: WebContext?, routing: Routing?, paramIndex: Int): Any {
        return delayFuture(vertx, millis) {
            orderTestRouter.list.add(javaClass.simpleName)
        }
    }
}

@Singleton
class OneSecondParamResolver @Inject constructor(vertx: Vertx, orderTestRouter: OrderTestRouter) : AbstractDelayedParamResolver(1000, vertx, orderTestRouter)

@Singleton
class ThreeSecondParamResolver @Inject constructor(vertx: Vertx, orderTestRouter: OrderTestRouter) : AbstractDelayedParamResolver(3000, vertx, orderTestRouter)

@Singleton
class FourSecondParamResolver @Inject constructor(vertx: Vertx, orderTestRouter: OrderTestRouter) : AbstractDelayedParamResolver(4000, vertx, orderTestRouter)

@Singleton
class HalfSecondResultProcessor @Inject constructor(val vertx: Vertx, val orderTestRouter: OrderTestRouter) : RoutingResultProcessor {
    override fun processResponse(context: WebContext?, routing: Routing?, returnValue: Any?): Any {
        return delayFuture(vertx, 500) {
            orderTestRouter.list.add(HalfSecondResultProcessor::class.java.simpleName)
        }
    }
}

@Singleton
class OneAndHalfSecondResultProcessor @Inject constructor(val vertx: Vertx, val orderTestRouter: OrderTestRouter) : RoutingResultProcessor {
    override fun processResponse(context: WebContext?, routing: Routing?, returnValue: Any?): Any {
        return delayFuture(vertx, 1500) {
            context as PlainHttpContext
            orderTestRouter.list.add(OneAndHalfSecondResultProcessor::class.java.simpleName)
        }
    }
}

fun delayFuture(vertx: Vertx, millis: Long, fn: () -> Unit) =
    Promise.promise<Any>().let { promise ->
        vertx.setTimer(millis) {
            fn()
            promise.complete(Any())
        }
        promise.future()
    }

@Singleton
class ErrorInParamResolverRouter {
    @Get("/errorInParamResolverTest")
    fun errorInParamResolverTest(
        @OneSecondParamResolverAnno param1: Any,
        @ErrorInParamResolverAnno param2: Any
    ) {
    }

    @On500("/**")
    @PlainText
    fun errorHandler(throwable: Throwable) = throwable.message
}

@Retention(AnnotationRetention.RUNTIME)
@Extends(ParameterResolver::class)
@ParameterResolver(resolver = ErrorInParamResolver::class)
annotation class ErrorInParamResolverAnno

class ErrorInParamResolver : RoutingParameterResolver<Any> {
    override fun resolveParameter(context: WebContext?, routing: Routing?, paramIndex: Int): Any {
        throw RuntimeException("unlucky!")
    }
}

@Singleton
class ParamNotAbleResolveErrorRouter {
    @Get("/ParamNotAbleResolveErrorRouter")
    fun test(
        @OneSecondParamResolverAnno param1: Any,
        unresolveableParam: Any
    ) {
    }
}

@ExtendWith(ForestExtension::class)
@ForestTest(appClass = RoutingInvocationIntegrationTestApp::class)
@DisableAutoScan
@IncludeComponents(classes = [OrderTestRouter::class, ErrorInParamResolverRouter::class, ParamNotAbleResolveErrorRouter::class])
class RoutingInvocationIntegrationTest : AbstractForestIntegrationTest() {
    @Inject
    lateinit var orderTestRouter: OrderTestRouter

    @Test
    fun `routing invocation is strictly ordered`() = runBlockingUnit {
        get("/orderTest").bodyAsString()
        assertEquals(
            listOf(
                OneSecondParamResolver::class.java.simpleName,
                ThreeSecondParamResolver::class.java.simpleName,
                FourSecondParamResolver::class.java.simpleName,
                OrderTestRouter::class.java.simpleName,
                OneAndHalfSecondResultProcessor::class.java.simpleName,
                HalfSecondResultProcessor::class.java.simpleName
            ),
            orderTestRouter.list
        )
    }

    @Test
    fun `exceptions in param resolvers can be caught`() = runBlockingUnit {
        get("/errorInParamResolverTest").bodyAsString().apply {
            MatcherAssert.assertThat(this, CoreMatchers.containsString("unlucky!"))
        }
    }

    @Test
    fun `exception can be caught if param can not be resolved`() = runBlockingUnit {
        get("/ParamNotAbleResolveErrorRouter").bodyAsString().apply {
            MatcherAssert.assertThat(this, CoreMatchers.containsString("Don't know how to resolve"))
        }
    }

    @Test
    fun `param resolvers can throw 4xx and 5xx HttpException`() {
    }

    fun `exceptions in result processor can be caught and later processors are not called`() {
    }
}
