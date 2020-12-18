package io.forestframework.testsupport

import com.google.inject.Module
import io.forestframework.core.ForestApplication
import io.forestframework.ext.api.After
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.ApplicationContext
import io.forestframework.testfixtures.DisableAutoScan
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import javax.inject.Inject

interface MockableService {
    fun foo(): String
}

object ProductionService : MockableService {
    override fun foo() = "Production"
}

class MyService @Inject constructor(val mockableService: MockableService) {
    fun foo() = mockableService.foo()
}

class ProductionServiceWiringExtension : Extension {
    override fun start(applicationContext: ApplicationContext) {
        applicationContext.modules.add(Module { binder ->
            binder.bind(MockableService::class.java).toInstance(ProductionService)
        })
    }
}

@WithExtensions(extensions = [ProductionServiceWiringExtension::class])
@ForestApplication
class MockBeanIntegrationTestApp

object TestMockService : MockableService {
    override fun foo() = "Mock"
}

@After(classes = [ProductionServiceWiringExtension::class])
class MockBeanExtension : Extension {
    override fun start(applicationContext: ApplicationContext) {
        applicationContext.modules.add(Module { binder ->
            binder.bind(MockableService::class.java).toInstance(TestMockService)
        })
    }
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = MockBeanIntegrationTestApp::class)
@DisableAutoScan
@WithExtensions(extensions = [MockBeanExtension::class])
class MockBeanIntegrationTest {
    @Inject
    lateinit var myService: MyService

    @Test
    fun `can overwrite production bean with mock bean`() {
        Assertions.assertEquals("Mock", myService.foo())
    }
}
