package io.forestframework.testfixtures

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.ext.api.EnableExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.StartupContext

@EnableExtensions(extensions = [DisableAutoScanExtension::class])
@Extends(EnableExtensions::class)
annotation class DisableAutoScan

class DisableAutoScanExtension : Extension {
    override fun beforeInjector(startupContext: StartupContext) {
        startupContext.componentClasses.removeIf {
            it.`package`.name.startsWith(startupContext.appClass.`package`.name) && it != startupContext.appClass
        }
    }
}
