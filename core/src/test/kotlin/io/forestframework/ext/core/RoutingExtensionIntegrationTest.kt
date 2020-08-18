package io.forestframework.ext.core

import io.forestframework.core.ForestApplication
import io.forestframework.core.http.Router
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.result.GetJson
import io.forestframework.testsupport.ForestTest

data class User(val id: Int)

@Router("/users")
class TestRouter {
    @GetJson
    fun getAllUsers() = listOf(User(111), User(222))

    @GetJson("/:userid")
    fun getUserById(@PathParam("userid") userId: Int) = User(userId)
}

@ForestApplication
class RouterTestApplication

@ForestTest(appClass = RouterTestApplication::class)
class RoutingExtensionIntegrationTest {
    fun `can define prefixed in Router`() {
        // get(/users)
        // get(/users/12345)
    }
}
