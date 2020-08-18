package io.forestframework.core.singletontest

import io.forestframework.core.Component
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.Router
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import javax.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ForestApplication
class ForestApplicationSingletonTestApp {
    @Inject
    lateinit var router1: Router1

    @GetPlainText("/appToStringFromApp")
    fun appToString() = toString()

    @GetPlainText("/router1")
    fun router1ToString() = router1.toString()
}

@Router
class Router1 @Inject constructor(val router2: Router2) {
    @Inject
    lateinit var app: ForestApplicationSingletonTestApp

    @GetPlainText("/appToStringFromRouter")
    fun appToString() = app.toString()

    @GetPlainText("/router2")
    fun router2ToString() = router2.toString()
}

@Router
class Router2 @Inject constructor(val component1: Component1) {
    @GetPlainText("/component1")
    fun component1ToString() = component1.toString()

    @GetPlainText("/component2")
    fun component2ToString() = component1.component2.toString()
}

@Component
class Component1 @Inject constructor(val component2: Component2)

@Component
class Component2

@ExtendWith(ForestExtension::class)
@ForestTest(appClass = ForestApplicationSingletonTestApp::class)
class ForestApplicationSingletonTest : AbstractForestIntegrationTest() {
    @Inject
    lateinit var app: ForestApplicationSingletonTestApp

    @Inject
    lateinit var router1: Router1

    @Inject
    lateinit var router2: Router2

    @Inject
    lateinit var component1: Component1

    @Inject
    lateinit var component2: Component2

    @Test
    fun `app instance is singleton`() = runBlockingUnit {
        val nameFromApp = get("/appToStringFromApp").assert200().bodyAsString()
        val nameFromRouter = get("/appToStringFromRouter").assert200().bodyAsString()
        val nameFromInject = app.toString()

        assertEquals(nameFromApp, nameFromRouter)
        assertEquals(nameFromApp, nameFromInject)
    }

    @Test
    fun `components are singleton`() = runBlockingUnit {
        assertEquals(router1.toString(), get("/router1").assert200().bodyAsString())
        assertEquals(router2.toString(), get("/router2").assert200().bodyAsString())
        assertEquals(component1.toString(), get("/component1").assert200().bodyAsString())
        assertEquals(component2.toString(), get("/component2").assert200().bodyAsString())
    }
}
