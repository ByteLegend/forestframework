package io.forestframework.testfixtures

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

class RedisSetUpExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext?) {
        System.setProperty("forest.redis.endpoints", "[\"redis://localhost:${System.getProperty(REDIS_PORT_PROPERTY)}\"]");
    }
}