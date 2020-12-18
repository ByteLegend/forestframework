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
class HeaderParameterResolverIntegrationTestApp {
    @GetPlainText("/NoneMatch")
    fun noneMatch(@Header("NoneMatch") header: String?) = Objects.toString(header)

    @GetPlainText("/OnlyOneMatch")
    fun onlyOneMatch(@Header("OnlyOneMatch") header: String) = header

    @GetPlainText("/OnlyOneMatchCaseInsensitive")
    fun onlyOneMatchCaseInsensitive(@Header("onlyonematch") header: String) = header

    @GetPlainText("/MultipleMatches")
    fun multipleMatches(@Header("MultipleMatches") header: String) = header
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = HeaderParameterResolverIntegrationTestApp::class)
@DisableAutoScan
class HeaderParameterResolverIntegrationTest : AbstractForestIntegrationTest() {
    @ParameterizedTest
    @CsvSource(value = [
        "none match, NoneMatch, null",
        "only one match, OnlyOneMatch, OnlyOneMatch",
        "only one match, OnlyOneMatchCaseInsensitive, OnlyOneMatch",
        "multiple matches, MultipleMatches, first"
    ])
    fun `can resolve header`(scenario: String, path: String, expectedResult: String) {
        println(scenario)
        send(HttpMethod.GET,
            "/$path",
            headers = mapOf("OnlyOneMatch" to "OnlyOneMatch"),
            multiHeaders = mapOf("MultipleMatches" to listOf("first", "second"))
        ).assertBody(expectedResult)
    }
}
