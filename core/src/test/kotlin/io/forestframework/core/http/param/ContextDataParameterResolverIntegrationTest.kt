package io.forestframework.core.http.param

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.HttpMethod
import io.forestframework.core.http.WebContext
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.core.http.routing.PreHandler
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@ForestApplication
class ContextDataParameterResolverIntegrationTestApp {
    @PreHandler("/string")
    fun putStringIntoContext(context: WebContext) {
        context.put("string", "abc")
    }

    @GetPlainText("/string")
    fun readStringFromContext(@ContextData("string") data: String?) = data

    @PreHandler("/int/**")
    fun putIntIntoContext(context: WebContext) {
        context.put("int", 123)
    }

    @GetPlainText("/int/correct")
    fun readIntFromContext(@ContextData("int") data: Int?) = data

    @GetPlainText("/int/incorrect")
    fun readIntFromContextIncorrect(@ContextData("int") data: String?) = data
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = ContextDataParameterResolverIntegrationTestApp::class)
@DisableAutoScan
class ContextDataParameterResolverIntegrationTest : AbstractForestIntegrationTest() {
    @ParameterizedTest
    @CsvSource(
        value = [
            "/string, abc",
            "/int/correct, 123"
        ]
    )
    fun `can resolve context data`(path: String, expectedResult: String) {
        send(HttpMethod.GET, "/$path").assertBody(expectedResult)
    }

    @Test
    fun `error if type mismatch`() {
        send(HttpMethod.GET, "/int/incorrect").assert500()
    }
}
