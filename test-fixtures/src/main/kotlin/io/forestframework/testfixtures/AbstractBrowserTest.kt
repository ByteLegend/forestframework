package io.forestframework.testfixtures

import io.forestframework.core.config.Config
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.openqa.selenium.Capabilities
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.testcontainers.Testcontainers
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.DefaultRecordingFileFactory
import org.testcontainers.lifecycle.TestDescription
import java.io.File
import java.lang.reflect.Method
import java.util.Optional
import javax.inject.Inject

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class BrowserTest

abstract class AbstractBrowserTest {
    protected val host = "host.testcontainers.internal"

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Inject
    @Config("http.port")
    lateinit var port: Integer

    @TestFactory
    fun testOnChromeAndFirefox(): Iterable<DynamicTest> =
        javaClass.methods
            .filter { it.isAnnotationPresent(BrowserTest::class.java) }
            .flatMap {
                listOf(
                    runOn(it, "firefox", FirefoxOptions()),
                    runOn(it, "chrome", ChromeOptions())
                )
            }

    private fun runOn(method: Method, browserName: String, browserOptions: Capabilities) = DynamicTest.dynamicTest("$method $browserName") {
        Testcontainers.exposeHostPorts(port.toInt())
        val browserWebDriverContainer = BrowserWebDriverContainer<Nothing>().apply {
            withCapabilities(browserOptions)
            if (System.getProperty("recording.dir") == null) {
                withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, null)
            } else {
                withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING, File(System.getProperty("recording.dir")))
                withRecordingFileFactory(DefaultRecordingFileFactory())
            }
        }
        try {
            browserWebDriverContainer.start()
            method.invoke(this, mapOf("driver" to browserWebDriverContainer.webDriver))
            browserWebDriverContainer.afterTest(toTestDescription(javaClass, method, browserName), Optional.empty())
        } catch (t: Throwable) {
            browserWebDriverContainer.afterTest(toTestDescription(javaClass, method, browserName), Optional.of(t))
            throw t
        } finally {
            browserWebDriverContainer.stop()
        }
    }

    private fun toTestDescription(klass: Class<*>, method: Method, browserName: String): TestDescription {
        return object : TestDescription {
            override fun getTestId(): String {
                return "${klass.simpleName}-${method.name}-$browserName"
            }

            override fun getFilesystemFriendlyName(): String {
                return "${klass.simpleName}-${method.name}-$browserName"
            }
        }
    }
}
