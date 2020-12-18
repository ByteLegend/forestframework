package io.forestframework.core.http.param

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.HttpMethod
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.Objects

@ForestApplication
class CookieParameterResolverIntegrationTestApp {
    @GetPlainText("/cookieValueWhenNoCookie")
    fun cookieValueWhenNoCookie(@Cookie("inexistent") cookieValue: String?) = Objects.toString(cookieValue)

    @GetPlainText("/myCookie")
    fun myCookie(@Cookie("myCookie") cookieValue: String) = cookieValue

    @GetPlainText("/yourCookie")
    fun yourCookie(@Cookie("yourCookie") cookieValue: String) = cookieValue
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = CookieParameterResolverIntegrationTestApp::class)
@DisableAutoScan
class CookieParameterResolverIntegrationTest : AbstractForestIntegrationTest() {
    @ParameterizedTest
    @CsvSource(value = [
        "/cookieValueWhenNoCookie, null",
        "/myCookie, myCookieValue",
        "/yourCookie, yourCookieValue"
    ])
    fun `can resolve cookie`(path: String, expectedResult: String) {
        send(HttpMethod.GET,
            "/$path",
            headers = mapOf("Cookie" to "myCookie=myCookieValue; yourCookie=yourCookieValue")
        ).assertBody(expectedResult)
    }
}
