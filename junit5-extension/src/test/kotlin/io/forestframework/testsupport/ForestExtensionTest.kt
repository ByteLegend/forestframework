package io.forestframework.testsupport

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.core.ForestApplication
import io.forestframework.core.config.ConfigProvider
import io.forestframework.ext.api.EnableExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.StartupContext
import io.forestframework.ext.core.AutoRoutingScanExtension
import io.forestframework.ext.core.AutoScanComponentsExtension
import io.forestframework.ext.core.BannerExtension
import io.forestframework.ext.core.ExtraConfig
import javax.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

class Extension1 : Extension
class Extension2 : Extension
class Extension3 : Extension
class Extension4 : Extension
class Extension5 : Extension
class Extension6 : Extension
class Extension7 : Extension

@Extends(EnableExtensions::class)
@EnableExtensions(extensions = [Extension1::class])
annotation class EnableExtension1

@Extends(EnableExtensions::class)
@EnableExtensions(extensions = [Extension2::class])
annotation class EnableExtension2

@Extends(EnableExtensions::class)
@EnableExtensions(extensions = [Extension4::class])
annotation class EnableExtension4

@Extends(EnableExtensions::class)
@EnableExtensions(extensions = [Extension5::class])
annotation class EnableExtension5

@Extends(EnableExtensions::class)
@EnableExtensions(extensions = [Extension7::class])
annotation class EnableExtension7

@EnableExtension2
@ForestApplication
@EnableExtensions(extensions = [Extension3::class])
@EnableExtension4
@EnableExtension5
class App

@ExtendWith(ForestExtension::class)
@EnableExtension1
@ForestTest(appClass = App::class)
@EnableExtensions(extensions = [Extension6::class])
@EnableExtension7
class ForestExtensionOrderTest {
    @Inject
    lateinit var startupContext: StartupContext

    @Test
    fun `extensions are strictly ordered`() {
        assertEquals(
            listOf(
                Extension1::class.java,
                Extension2::class.java,
                BannerExtension::class.java,
                AutoScanComponentsExtension::class.java,
                AutoRoutingScanExtension::class.java,
                Extension3::class.java,
                Extension4::class.java,
                Extension5::class.java,
                BindFreePortExtension::class.java,
                Extension6::class.java,
                Extension7::class.java
            ),
            startupContext.extensions.map { it.javaClass })
    }
}

@ForestApplication
@ExtraConfig(value = ["forest.order.test=1"])
class AppWithExtraConfig

@ExtendWith(ForestExtension::class)
@ForestTest(appClass = AppWithExtraConfig::class)
@ExtraConfig(value = ["forest.order.test=2"])
class TestExtraConfigOverwritingAppExtraConfigTest {
    @Inject
    lateinit var configProvider: ConfigProvider

    @Test
    fun `ExtraConfig on AppTest overwrites that on App`() {
        assertEquals(2, configProvider.getInstance("forest.order.test", Integer::class.java))
    }
}

@ExtendWith(ForestExtension::class)
@ExtraConfig(value = ["forest.order.test=2"])
@ForestTest(appClass = AppWithExtraConfig::class)
class TestExtraConfigNotOverwritingAppExtraConfigText {
    @Inject
    lateinit var configProvider: ConfigProvider

    @Test
    fun `ExtraConfig on App overwrites that on AppTest`() {
        assertEquals(1, configProvider.getInstance("forest.order.test", Integer::class.java))
    }
}
