package io.forestframework.core.http.param

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestIntegrationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

enum class PathParamResolverIntegrationTestEnum {
    AAA, BBB, CCC
}

@ForestApplication
class PathParamResolverIntegrationTestApp {
    @GetPlainText("/boolean/:value")
    fun getBoolean(@PathParam("value") value: Boolean) = value.toString()

    @GetPlainText("/int/:value")
    fun getInt(@PathParam("value") value: Int) = value.toString()

    @GetPlainText("/long/:value")
    fun getLong(@PathParam("value") value: Long) = value.toString()

    @GetPlainText("/enum/:value")
    fun getEnum(@PathParam("value") value: PathParamResolverIntegrationTestEnum) = value.name.toLowerCase()

    @GetPlainText("/string/:value")
    fun getString(@PathParam("value") value: String) = value
}

@ExtendWith(ForestExtension::class)
@ForestIntegrationTest(appClass = PathParamResolverIntegrationTestApp::class)
@DisableAutoScan
class PathParamResolverIntegrationTest : AbstractForestIntegrationTest() {
    @ParameterizedTest(name = "can convert path param value for {1}")
    @CsvSource(value = [
        "/boolean/true,     true",
        "/boolean/false,    false",
        "/int/1234,         1234",
        "/int/-1234,        -1234",
        "/long/2212345678,  2212345678",
        "/enum/AAA,         aaa"
    ])
    fun `can convert path param`(url: String, expected: String) = runBlockingUnit {
        get(url).assert200().apply { Assertions.assertEquals(expected, bodyAsString()) }
    }
}
