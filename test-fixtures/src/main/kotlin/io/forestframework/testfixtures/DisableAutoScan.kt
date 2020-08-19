package io.forestframework.testfixtures

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.ext.api.After
import io.forestframework.ext.api.EnableExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.StartupContext
import io.forestframework.ext.core.AutoScanComponentsExtension

@EnableExtensions(extensions = [DisableAutoScanExtension::class])
@Extends(EnableExtensions::class)
annotation class DisableAutoScan

@After(classes = [AutoScanComponentsExtension::class])
class DisableAutoScanExtension : Extension {
    override fun beforeInjector(startupContext: StartupContext) {
        startupContext.componentClasses.removeIf {
            it.`package`.name.startsWith(startupContext.appClass.`package`.name) && it != startupContext.appClass
        }
    }
}
