package io.forestframework.testfixtures

import geb.Configuration
import io.forestframework.core.config.Config
import java.lang.reflect.Method
import javax.inject.Inject
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class BrowserTest

abstract class AbstractBrowserTest {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Inject
    @Config("forest.http.port")
    lateinit var port: Integer

    @TestFactory
    fun testOnChromeAndFirefox(): Iterable<DynamicTest> =
        javaClass.methods
            .filter { it.isAnnotationPresent(BrowserTest::class.java) }
            .flatMap {
                listOf(
                    // https://github.com/actions/virtual-environments/blob/main/images/linux/Ubuntu2004-README.md
                    runOn(it, "firefox", "GECKOWEBDRIVER", "gecko", "org.openqa.selenium.firefox.FirefoxDriver"),
                    runOn(it, "chrome", "CHROMEWEBDRIVER", "chrome", "org.openqa.selenium.chrome.ChromeDriver")
                )
            }

    private fun runOn(method: Method, browserName: String, envName: String, driverName: String, driverClass: String) = DynamicTest.dynamicTest("$method $browserName") {
        Assumptions.assumeFalse(System.getenv(envName).isEmpty())
        System.setProperty("webdriver.$driverName.driver", "${System.getenv(envName)}/${driverName}driver")
        val conf = Configuration(mapOf("driver" to driverClass))
        method.invoke(this, conf)
    }
}
