package io.forestframework.core.aop

import com.google.inject.AbstractModule
import com.google.inject.ImplementedBy
import com.google.inject.Inject
import com.google.inject.Singleton
import com.google.inject.matcher.Matchers
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.param.QueryParam
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.ApplicationContext
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.lang.reflect.Method

annotation class Log(val level: String)

class LogAopModule : AbstractModule() {
    private val logger: Logger = Logger()
    override fun configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Log::class.java), object : MethodInterceptor {
            override fun invoke(methodInvocation: MethodInvocation): Any {
                val arguments: Array<Any> = methodInvocation.arguments
                val method: Method = methodInvocation.method

                logger.log(method.getAnnotation(Log::class.java).level, "enter ${method.name} method: ${arguments.contentToString()}")
                return methodInvocation.proceed()
            }
        })

        bind(Logger::class.java).toInstance(logger)
    }
}

class Logger {
    val logs: MutableMap<String, MutableList<String>> = mutableMapOf()
    fun log(level: String, message: String) {
        logs.computeIfAbsent(level) { mutableListOf() }.add(message)
    }
}

class LogAopExtension : Extension {
    override fun start(applicationContext: ApplicationContext) {
        applicationContext.modules.add(LogAopModule())
    }
}

@ForestApplication
@DisableAutoScan
@WithExtensions(extensions = [LogAopExtension::class])
class LoggingAopApp @Inject constructor(private val interfacedService: InterfacedService, private val classedService: ClassedService) {
    @GetPlainText("/interfaced")
    fun interfaced(@QueryParam("param1") param1: String, @QueryParam("param2") param2: Int) = interfacedService.interfaced(param1, param2)

    @GetPlainText("/classed")
    fun classed(@QueryParam("param1") param1: String, @QueryParam("param2") param2: Int) = classedService.classed(param1, param2)
}

@ImplementedBy(InterfacedServiceImpl::class)
interface InterfacedService {
    fun interfaced(param1: String, param2: Int): String
}

open class InterfacedServiceImpl : InterfacedService {
    @Log("debug")
    override fun interfaced(param1: String, param2: Int) = "$param1 $param2"
}

@Singleton
open class ClassedService {
    @Log("info")
    open fun classed(param1: String, param2: Int) = "$param1 $param2"
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = LoggingAopApp::class)
class LoggingAopIntegrationTest : AbstractForestIntegrationTest() {
    @Inject
    lateinit var logger: Logger

    @ParameterizedTest
    @CsvSource(value = [
        "interfaced, debug",
        "classed, info"
    ])
    fun `can use guava AOP`(endpoint: String, level: String) = runBlockingUnit {
        get("/$endpoint?param1=test&param2=123")
            .assert200()
            .assertBody("test 123")

        Assertions.assertTrue(logger.logs.getValue(level).contains("enter $endpoint method: [test, 123]"))
    }
}
