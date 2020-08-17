package io.forestframework.testfixtures

import geb.Configuration
import io.forestframework.core.config.Config
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.lang.reflect.Method
import javax.inject.Inject

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class EndToEndTest

abstract class AbstractEndToEndTest {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Inject
    @Config("forest.http.port")
    lateinit var port: Integer

    @TestFactory
    fun testOnChromeAndFirefox(): Iterable<DynamicTest> =
        javaClass.methods
            .filter { it.isAnnotationPresent(EndToEndTest::class.java) }
            .flatMap {
                listOf(
                    //https://github.com/actions/virtual-environments/blob/c647d2d4ef048c2bb9d98df65a3a05686a095d8e/images/linux/scripts/installers/firefox.sh
                    runOn(it, "firefox", "GECKODRIVER_BIN", "webdriver.gecko.driver", "org.openqa.selenium.firefox.FirefoxDriver"),
                    // https://github.com/actions/virtual-environments/blob/970e8f5c4f87515f5c75e569a7eb467d3a9e5ae5/images/linux/scripts/installers/google-chrome.sh#L43
                    runOn(it, "chrome", "CHROMEDRIVER_BIN", "webdriver.chrome.driver", "org.openqa.selenium.chrome.ChromeDriver")
                )
            }

    private fun runOn(method: Method, browserName: String, envName: String, systemPropertyName: String, driver: String) = DynamicTest.dynamicTest("$method $browserName") {
        Assumptions.assumeFalse(System.getenv(envName) == null)
        System.setProperty(systemPropertyName, System.getenv(envName))
        val conf = Configuration(mapOf("driver" to driver))
        method.invoke(this, conf)
    }
}