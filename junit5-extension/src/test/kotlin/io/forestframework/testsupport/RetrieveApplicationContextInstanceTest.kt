package io.forestframework.testsupport

import io.forestframework.core.ForestApplication
import io.forestframework.core.config.Config
import io.forestframework.ext.api.ApplicationContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import javax.inject.Inject

@ForestApplication
class RetrieveApplicationContextInstanceTestApp

class SetupAbcExtension : BeforeAllCallback {
    override fun beforeAll(extensionContext: ExtensionContext) {
        extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
            .get(ApplicationContext::class.java, ApplicationContext::class.java)
            .configProvider.addConfig("a.b.c", "1")
    }
}

@ForestIntegrationTest(appClass = RetrieveApplicationContextInstanceTestApp::class)
@ExtendWith(value = [SetupAbcExtension::class])
open class GrandParentClass

class SetupDefExtension : BeforeAllCallback {
    override fun beforeAll(extensionContext: ExtensionContext) {
        extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
            .get(ApplicationContext::class.java, ApplicationContext::class.java)
            .configProvider.addConfig("d.e.f", "2")
    }
}

@ExtendWith(value = [SetupDefExtension::class])
open class ParentClass : GrandParentClass()

class RetrieveApplicationContextInstanceTest : ParentClass() {
    @Config("a.b.c")
    @Inject
    lateinit var abc: String

    @Config("d.e.f")
    @Inject
    lateinit var def: String

    @Test
    fun `can retrieve ApplicationContext instance from ExtensionContext`() {
        Assertions.assertEquals("1", abc)
        Assertions.assertEquals("2", def)
    }
}
