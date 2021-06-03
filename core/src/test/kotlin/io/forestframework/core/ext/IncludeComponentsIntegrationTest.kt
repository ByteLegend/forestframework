package io.forestframework.core.ext

import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.ext.api.ApplicationContext
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.core.IncludeComponents
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject
import javax.inject.Singleton

val events = mutableListOf<String>()

@ForestApplication
@DisableAutoScan
@IncludeComponents(classes = [EagerCreated::class], eager = true)
class IncludeComponentsIntegrationTestApp1 @Inject constructor(
    val eagerCreated: EagerCreated,
    val lazyCreated: LazyCreated
) {
    @GetPlainText("/app1")
    fun empty() = ""
}

@ForestApplication
@DisableAutoScan
@IncludeComponents(classes = [EagerCreated::class], eager = true)
@WithExtensions(extensions = [MyExtension::class])
class IncludeComponentsIntegrationTestApp2 @Inject constructor(
    val eagerCreated: EagerCreated,
    val lazyCreated: LazyCreated
) {
    @GetPlainText("/app2")
    fun empty() = ""
}

class MyExtension : Extension {
    override fun start(applicationContext: ApplicationContext) {
        applicationContext.components.add(LazyCreated::class.java)
    }
}

@Singleton
class EagerCreated {
    init {
        events.add("EagerCreated")
    }
}

@Singleton
class LazyCreated {
    init {
        events.add("LazyCreated")
    }
}

class IncludeComponentsIntegrationTest : AbstractForestIntegrationTest() {
    @BeforeEach
    fun beforeEach() {
        events.clear()
    }

    @Test
    fun `can create components eagerly`() {
        Forest.run(IncludeComponentsIntegrationTestApp1::class.java).use {
            port = it.configProvider.getInstance("http.port", Integer::class.java)
            Assertions.assertEquals(listOf("EagerCreated"), events)
            get("/app1").assert200()
            Assertions.assertEquals(listOf("EagerCreated", "LazyCreated"), events)
        }
    }

    @Test
    fun `can mix eager and non eager classes`() {
        Forest.run(IncludeComponentsIntegrationTestApp2::class.java).use {
            port = it.configProvider.getInstance("http.port", Integer::class.java)
            Assertions.assertEquals(listOf("EagerCreated"), events)
            get("/app2").assert200()
            Assertions.assertEquals(listOf("EagerCreated", "LazyCreated"), events)
        }
    }
}
