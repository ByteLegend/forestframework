package io.forestframework.ext.core

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.Router
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.result.GetJson
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.testfixtures.AbstractForestIntegrationTest
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import io.vertx.kotlin.ext.web.client.sendAwait
import org.hamcrest.MatcherAssert
import org.hamcrest.core.StringContains
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource


data class User(val id: Int)

@Router("/users")
class TestRouter {
    @GetJson
    fun getAllUsers() = listOf(User(111), User(222))

    @GetJson("/:userid")
    fun getUserById(@PathParam("userid") userId: Int) = User(userId)
}


@ForestApplication
class RouterTestApplication {
}

@ForestTest(appClass = RouterTestApplication::class)
class RoutingExtensionIntegrationTest {
    fun `can define prefixed in Router`() {
        // get(/users)
        // get(/users/12345)
    }
}
