package io.forestframework.testfixtures

import com.github.blindpirate.annotationmagic.Extends
import io.forestframework.ext.api.After
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.api.Extension
import io.forestframework.ext.api.ApplicationContext
import io.forestframework.ext.core.AutoComponentScanExtension

@WithExtensions(extensions = [DisableAutoScanExtension::class])
@Extends(WithExtensions::class)
annotation class DisableAutoScan

@After(classes = [AutoComponentScanExtension::class])
class DisableAutoScanExtension : Extension {
    override fun start(applicationContext: ApplicationContext) {
        applicationContext.components.removeIf {
            it.`package`.name.startsWith(applicationContext.appClass.`package`.name) && it != applicationContext.appClass
        }
    }
}
