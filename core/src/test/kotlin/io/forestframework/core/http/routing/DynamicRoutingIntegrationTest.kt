package io.forestframework.core.http.routing

import com.google.inject.Injector
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.HttpMethod
import io.forestframework.core.http.HttpResponse
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.ext.api.After
import io.forestframework.ext.api.Before
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.core.AutoRoutingScanExtension
import io.forestframework.ext.core.HttpServerExtension
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

class DynamicAddingRoutingExtension : Extension {
    override fun configure(injector: Injector) {
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

@Before(classes = [HttpServerExtension::class])
@After(classes = [AutoRoutingScanExtension::class])
class DynamicAddRoutingToExistingRoutingExtension : Extension {
    override fun configure(injector: Injector) {
        injector.getInstance(RoutingManager::class.java)
            .getRouting(RoutingType.HANDLER)
            .forEach {
                injector.getInstance(RoutingManager::class.java)
                    .getRouting(RoutingType.POST_HANDLER)
                    .add(DefaultRouting(
                        false,
                        RoutingType.POST_HANDLER,
                        it.path,
                        "",
                        listOf(HttpMethod.GET),
                        DynamicRoutingIntegrationTestApp::class.java.methods.first { it.name == "dynamicPostHandler" },
                        0,
                        listOf("*/*"),
                        listOf("*/*")
                    ))
            }
    }
}

@WithExtensions(extensions = [DynamicAddRoutingToExistingRoutingExtension::class])
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

    fun dynamicPostHandler(response: HttpResponse) {
        response.writeLater("dynamicpost")
    }

    @GetPlainText("/login")
    fun mainHandler() = "main_"

    @GetPlainText("/index")
    fun index() = "index_"
}

@ExtendWith(ForestExtension::class)
@WithExtensions(extensions = [DynamicAddingRoutingExtension::class])
@ForestIntegrationTest(appClass = DynamicRoutingIntegrationTestApp::class)
@DisableAutoScan
class DynamicRoutingIntegrationTest : AbstractForestIntegrationTest() {
    @Test
    fun canHaveDynamicallyAddedPrePostHandler() {
        get("/login").assert200().assertBody("static_dynamic_main_dynamicpost")
    }

    @Test
    fun canHaveDynamicallyAddedPreHandler() {
        get("/index").assert200().assertBody("static_index_dynamicpost")
    }
}
