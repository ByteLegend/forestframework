package io.forestframework.core.http.routing

import com.google.inject.Injector
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.HttpMethod
import io.forestframework.core.http.HttpResponse
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.ext.api.EnableExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

class DynamicAddingRoutingExtension : Extension {
    override fun afterInjector(injector: Injector) {
        injector.getInstance(RoutingManager::class.java)
            .getRouting(RoutingType.PRE_HANDLER)
            .add(DefaultRouting(
                false,
                RoutingType.PRE_HANDLER,
                "/login",
                "",
                listOf(HttpMethod.GET),
                DynamicRoutingIntegrationTestApp::class.java.methods.first { it.name == "dynamicPreHandler" },
                0,
                listOf("*/*"),
                listOf("*/*")
            ))
    }
}

@ForestApplication
class DynamicRoutingIntegrationTestApp {
    @PreHandler("/**", order = -42)
    fun staticPrehandler(response: HttpResponse): Boolean {
        response.writeLater("static_")
        return true
    }

    fun dynamicPreHandler(response: HttpResponse) {
        response.writeLater("dynamic_")
    }

    @GetPlainText("/login")
    fun mainHandler() = "main"
}

@ExtendWith(ForestExtension::class)
@EnableExtensions(extensions = [DynamicAddingRoutingExtension::class])
@ForestTest(appClass = DynamicRoutingIntegrationTestApp::class)
@DisableAutoScan
class DynamicRoutingIntegrationTest : AbstractForestIntegrationTest() {
    @Test
    fun canHaveDynamicallyAddedPreHandler() {
        get("/login").assert200().assertBody("static_dynamic_main")
    }
}
