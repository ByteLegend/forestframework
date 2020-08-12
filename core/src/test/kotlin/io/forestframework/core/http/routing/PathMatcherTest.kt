package io.forestframework.core.http.routing

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PathMatcherTest {
    @ParameterizedTest
    @CsvSource(value = [
        "/chatroom/:userId/message,    /chatroom/10/message,  userId,   10",
        "/user/:userId,                /user/12345,           userId,   12345",
        "/user/:userName,              /user/Alice,           userName, Alice",
        "/user/:userId/order/:orderId, /user/1/order/200,     userId,   1",
        "/user/:userId/order/:orderId, /user/1/order/200,     orderId,  200",
        "/users/*,                     /users/12345,          *,        12345",
        "/users/*,                     /users/id/12345,       *,        id/12345"

    ])
    fun `can parse parameter from path patterns`(pattern: String, actualPath: String, paramName: String, expectedParamValue: String) {
        val pathMatcher = PathMatcher.fromPattern(pattern)

        Assertions.assertEquals(expectedParamValue, pathMatcher.pathParam(actualPath, paramName))
    }

    @ParameterizedTest(name = "match {1} for pattern {0}")
    @CsvSource(value = [
        "/index,                       /index,                true",
        "/index,                       /index2,               false",
        "/chatroom/:userId/message,    /chatroom/10/message,  true",
        "/chatroom/:userId/message,    /chatroom/10,          false",
        "/user/:userId,                /user/12345,           true",
        "/user/:userId,                /user/12345/id,        false",
        "/user/:userId/order/:orderId, /user/1/order/200,     true",
        "/user/*/order/*,              /user/1/order/200,     true",
        "/user/*/*/*,                  /user/1/order/200,     true",
        "/user/:userId/order/:orderId, /user/1/order/200,     true",
        "/users/*,                     /users/12345,          true",
        "/users/*,                     /users/id/12345,       true"

    ])
    fun `can match path`(pattern: String, actualPath: String, match: Boolean) {
        val pathMatcher = PathMatcher.fromPattern(pattern)

        Assertions.assertEquals(match, pathMatcher.matches(actualPath))
    }

    @Test
    fun `throw exception when more than one star in path`() {
        assertThrows<IllegalArgumentException> {
            PathMatcher.fromPattern("/user/*/order/*").pathParam("/user/1/order/2", "*")
        }
    }
}