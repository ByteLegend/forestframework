@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package io.forestframework.core.http

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.staticresource.GetStaticResource
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testfixtures.DisableAutoScan
import io.forestframework.testfixtures.runBlockingUnit
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Paths

@ForestApplication
class GetStaticResourceIntegrationTestApp {
    @GetStaticResource("/viaAbsolutePath")
    fun viaAbsolutePath() = File(javaClass.classLoader.getResource("GetStaticResourceIntegrationTestData/test.txt").toURI()).absolutePath

    @GetStaticResource("/viaPathRelativeToCwd")
    fun viaPathRelativeToCwd(): String {
        val target = File(javaClass.classLoader.getResource("GetStaticResourceIntegrationTestData/test.txt").toURI()).absoluteFile.toPath()
        val cwd = Paths.get(".").toAbsolutePath().normalize()
        return cwd.relativize(target).toString()
    }

    @GetStaticResource("/viaPathRelativeToClasspathEntry")
    fun viaPathRelativeToClasspathEntry() = "GetStaticResourceIntegrationTestData/test.txt"

    @GetStaticResource("/errorCodePath")
    fun errorCodePath() = ""
}

@ExtendWith(ForestExtension::class)
@ForestTest(appClass = GetStaticResourceIntegrationTestApp::class)
@DisableAutoScan
class GetStaticResourceIntegrationTest : AbstractForestIntegrationTest() {
    @ParameterizedTest(name = "can get static resource via {0}")
    @ValueSource(strings = ["viaAbsolutePath", "viaPathRelativeToCwd", "viaPathRelativeToClasspathEntry"])
    fun `get static resource`(url: String) = runBlockingUnit {
        get(url)
            .assert200()
            .assertContentType("text/plain;charset=UTF-8")
            .apply {
                Assertions.assertEquals("hello", bodyAsString())
            }
    }

    @Test
    fun `get 404 if handler returns empty string`() = runBlockingUnit {
        get("/errorCodePath").assert404()
    }
}
