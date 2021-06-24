package io.forestframework.testsupport

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.core.ForestApplication
import io.forestframework.core.config.ConfigProvider
import io.forestframework.ext.api.After
import io.forestframework.ext.api.ApplicationContext
import io.forestframework.ext.api.Before
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.core.AutoComponentScanExtension
import io.forestframework.ext.core.AutoRoutingScanExtension
import io.forestframework.ext.core.BannerExtension
import io.forestframework.ext.core.ExtraConfig
import io.forestframework.ext.core.HttpServerExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.inject.Inject

class Extension1 : Extension

@After(classes = [Extension1::class])
@Before(classes = [BannerExtension::class])
class Extension2 : Extension

@After(classes = [HttpServerExtension::class])
class Extension3 : Extension

@After(classes = [Extension3::class])
class Extension4 : Extension

@After(classes = [Extension4::class])
@Before(classes = [BindFreePortExtension::class])
class Extension5 : Extension

@After(classes = [BindFreePortExtension::class])
class Extension6 : Extension
@After(classes = [Extension6::class])
class Extension7 : Extension

@Extends(WithExtensions::class)
@WithExtensions(extensions = [Extension1::class])
annotation class EnableExtension1

@Extends(WithExtensions::class)
@WithExtensions(extensions = [Extension2::class])
annotation class EnableExtension2

@Extends(WithExtensions::class)
@WithExtensions(extensions = [Extension4::class])
annotation class EnableExtension4

@Extends(WithExtensions::class)
@WithExtensions(extensions = [Extension5::class])
annotation class EnableExtension5

@Extends(WithExtensions::class)
@WithExtensions(extensions = [Extension7::class])
annotation class EnableExtension7

@EnableExtension2
@ForestApplication
@WithExtensions(extensions = [Extension3::class])
@EnableExtension4
@EnableExtension5
class App

@EnableExtension1
@ForestIntegrationTest(appClass = App::class)
@WithExtensions(extensions = [Extension6::class])
@EnableExtension7
class ForestExtensionOrderTest {
    @Inject
    lateinit var applicationContext: ApplicationContext

    @Test
    fun `extensions are strictly ordered`() {
        assertEquals(
            listOf(
                Extension1::class.java,
                Extension2::class.java,
                BannerExtension::class.java,
                AutoComponentScanExtension::class.java,
                AutoRoutingScanExtension::class.java,
                HttpServerExtension::class.java,
                Extension3::class.java,
                Extension4::class.java,
                Extension5::class.java,
                BindFreePortExtension::class.java,
                Extension6::class.java,
                Extension7::class.java
            ),
            applicationContext.extensions.map { it.javaClass })
    }
}

@ForestApplication
@ExtraConfig(value = ["order.test=1"])
class AppWithExtraConfig

@ForestIntegrationTest(appClass = AppWithExtraConfig::class)
@ExtraConfig(value = ["order.test=2"])
class TestExtraConfigOverwritingAppExtraConfigTest {
    @Inject
    lateinit var configProvider: ConfigProvider

    @Test
    fun `ExtraConfig on AppTest overwrites that on App`() {
        assertEquals(2, configProvider.getInstance("order.test", Integer::class.java))
    }
}
