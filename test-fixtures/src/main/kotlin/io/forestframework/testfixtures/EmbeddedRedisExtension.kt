package io.forestframework.testfixtures

import io.forestframework.testsupport.utils.FreePortFinder
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import redis.embedded.RedisServer

/**
 * This is a fork of https://github.com/SVT/junit5-redis-extension , with extra fix for
 * https://github.com/kstyrc/embedded-redis/issues/51#issuecomment-318221233
 */
const val REDIS_URI_PROPERTY = "redis.uri"
const val REDIS_PORT_PROPERTY = "embedded-redis.port"

class EmbeddedRedisExtension(private val reusePort: Boolean = false) : BeforeAllCallback {
    lateinit var redisServer: RedisServer

    override fun beforeAll(context: ExtensionContext) {
        val port =
            if (!reusePort) FreePortFinder.findFreeLocalPort()
            else findPortFromSystemProperty() ?: FreePortFinder.findFreeLocalPort()
        System.setProperty(REDIS_PORT_PROPERTY, port.toString())
        redisServer = RedisServer.builder().apply {
            port(port)
            setting("maxmemory 128M")
        }.build()
        redisServer.start()

        val wrapper = RedisWrapper(redisServer)
        context.getStore(ExtensionContext.Namespace.create(EmbeddedRedisExtension::class.java))
            .put("redis", wrapper)
        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            wrapper.close()
        }))
    }

    class RedisWrapper(private val redis: RedisServer) : ExtensionContext.Store.CloseableResource {
        override fun close() {
            if (redis.isActive) {
                redis.stop()
            }
        }
    }
}

private val redisPortRegex = "([0-9]+)".toRegex()

fun findPortFromSystemProperty(): Int? {
    val uri = System.getProperty(REDIS_PORT_PROPERTY) ?: return null
    return redisPortRegex.matchEntire(uri)?.groups?.get(1)?.value?.toInt()
}
