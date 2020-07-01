package io.forestframework.core.config

import io.vertx.core.http.Http2Settings
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetClientOptions
import io.vertx.redis.client.RedisClientType
import io.vertx.redis.client.RedisOptions
import io.vertx.redis.client.RedisRole
import io.vertx.redis.client.RedisSlaves
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Collections.emptyList
import java.util.concurrent.TimeUnit

class ConfigProviderTest {
    @Test
    fun `can read raw property`() {
        val yaml = """
aaa:
  bbb:
    ccc:
      stringValue1: "" 
      stringValue2: "This is a string" 
      intValue: 42 
      listValue:
        - "a"
        - 42
        - "c"
    """.trimIndent()
        val provider = ConfigProvider.fromYaml(yaml)
        assertEquals("", provider.getInstance("aaa.bbb.ccc.stringValue1", String::class.java))
        assertEquals("", provider.getInstance("aaa.bbb.ccc", JsonObject::class.java).getString("stringValue1"))
        assertEquals("This is a string", provider.getInstance("aaa.bbb.ccc.stringValue2", String::class.java))
        assertEquals(42, provider.getInstance("aaa.bbb.ccc.intValue", Integer::class.java))
        assertEquals(42, provider.getInstance("aaa.bbb.ccc", JsonObject::class.java).getInteger("intValue"))
        assertEquals(null, provider.getInstance("aaa.notexist", Integer::class.java))
        assertEquals(null, provider.getInstance("aaa.notexist.notexist", Boolean::class.java))
        assertEquals(null, provider.getInstance("aaa.notexist.notexist", HttpServerOptions::class.java))
    }

    @Test
    fun `can read Options property`() {
        val yaml = """
forest:
  http:
    port: 12345
    compressionSupported: true
    initialSettings:
      pushEnabled: false
      maxHeaderListSize: 0x7ffffffe
  redis:
    endpoints:
      - redis://localhost:6380
    netClientOptions:
      sslHandshakeTimeout: 1
      sslHandshakeTimeoutUnit: MINUTES
      enabledCipherSuites:
        - a
        - b
      crlPaths:
        - aaa
      enabledSecureTransportProtocols:
        - TLSv1
      tcpQuickAck: true
    role: SENTINEL
    useSlave: ALWAYS
    type: CLUSTER
    maxPoolSize: 10
"""
        val provider = ConfigProvider.fromYaml(yaml)

        val httpJsonObject = provider.getInstance("forest.http", JsonObject::class.java)
        assertEquals(12345, httpJsonObject.getInteger("port"))

        val httpServerOptions: HttpServerOptions = provider.getInstance("forest.http", HttpServerOptions::class.java)
        assertEquals(12345, httpServerOptions.port)
        assertEquals(12345, provider.getInstance("forest.http.port", Integer::class.java))
        assertEquals(true, httpServerOptions.isCompressionSupported)
        assertEquals(true, provider.getInstance("forest.http.compressionSupported", Boolean::class.java))
        assertEquals(false, httpServerOptions.initialSettings.isPushEnabled)
        assertEquals(false, provider.getInstance("forest.http.initialSettings.pushEnabled", Boolean::class.java))
        assertEquals(0x7ffffffe, httpServerOptions.initialSettings.maxHeaderListSize)
        assertEquals(0x7ffffffe, provider.getInstance("forest.http.initialSettings.maxHeaderListSize", Integer::class.java))

        val redisOptions: RedisOptions = provider.getInstance("forest.redis", RedisOptions::class.java)

        assertEquals(1, redisOptions.netClientOptions.sslHandshakeTimeout)
        assertEquals(1, provider.getInstance("forest.redis.netClientOptions.sslHandshakeTimeout", Integer::class.java))
        assertEquals(TimeUnit.MINUTES, redisOptions.netClientOptions.sslHandshakeTimeoutUnit)
        assertEquals(TimeUnit.MINUTES, provider.getInstance("forest.redis.netClientOptions.sslHandshakeTimeoutUnit", TimeUnit::class.java))
        assertEquals(listOf("a", "b"), ArrayList(redisOptions.netClientOptions.enabledCipherSuites))
        assertEquals(listOf("a", "b"), provider.getInstance("forest.redis.netClientOptions.enabledCipherSuites", List::class.java))
        assertEquals(listOf("a", "b"), provider.getInstance("forest.redis.netClientOptions.enabledCipherSuites", ArrayList::class.java))
        assertEquals(listOf("aaa"), redisOptions.netClientOptions.crlPaths)
        assertEquals(listOf("aaa"), provider.getInstance("forest.redis.netClientOptions.crlPaths", List::class.java))
        assertEquals(listOf("aaa"), provider.getInstance("forest.redis.netClientOptions.crlPaths", ArrayList::class.java))
        assertEquals(listOf("TLSv1"), ArrayList(redisOptions.netClientOptions.enabledSecureTransportProtocols))
        assertEquals(listOf("TLSv1"), provider.getInstance("forest.redis.netClientOptions.enabledSecureTransportProtocols", List::class.java))
        assertEquals(true, redisOptions.netClientOptions.isTcpQuickAck)
        assertEquals(true, provider.getInstance("forest.redis.netClientOptions.tcpQuickAck", Boolean::class.java))

        assertEquals(listOf("redis://localhost:6380"), redisOptions.endpoints)
        assertEquals(listOf("redis://localhost:6380"), provider.getInstance("forest.redis.endpoints", List::class.java))
        assertEquals(RedisRole.SENTINEL, redisOptions.role)
        assertEquals(RedisRole.SENTINEL, provider.getInstance("forest.redis.role", RedisRole::class.java))
        assertEquals(RedisSlaves.ALWAYS, redisOptions.useSlave)
        assertEquals(RedisSlaves.ALWAYS, provider.getInstance("forest.redis.useSlave", RedisSlaves::class.java))
        assertEquals(RedisClientType.CLUSTER, redisOptions.type)
        assertEquals(RedisClientType.CLUSTER, provider.getInstance("forest.redis.type", RedisClientType::class.java))
        assertEquals(10, redisOptions.maxPoolSize)
        assertEquals(10, provider.getInstance("forest.redis.maxPoolSize", Integer::class.java))
    }

    @Test
    fun `return default value if not defined`() {
        val provider = ConfigProvider.fromYaml("")

        val httpServerOptions: HttpServerOptions = provider.getInstance("forest.http", HttpServerOptions::class.java)
        assertEquals(HttpServerOptions.DEFAULT_PORT, httpServerOptions.port)
        assertEquals(HttpServerOptions.DEFAULT_COMPRESSION_SUPPORTED, httpServerOptions.isCompressionSupported)
        assertEquals(Http2Settings.DEFAULT_ENABLE_PUSH, httpServerOptions.initialSettings.isPushEnabled)
        assertEquals(Http2Settings.DEFAULT_MAX_HEADER_LIST_SIZE.toLong(), httpServerOptions.initialSettings.maxHeaderListSize)
        assertEquals(null, httpServerOptions.initialSettings.extraSettings)

        val redisOptions: RedisOptions = provider.getInstance("forest.redis", RedisOptions::class.java)

        assertEquals(NetClientOptions.DEFAULT_SSL_HANDSHAKE_TIMEOUT, redisOptions.netClientOptions.sslHandshakeTimeout)
        assertEquals(NetClientOptions.DEFAULT_SSL_HANDSHAKE_TIMEOUT_TIME_UNIT, redisOptions.netClientOptions.sslHandshakeTimeoutUnit)
        assertEquals(emptySet<String>(), redisOptions.netClientOptions.enabledCipherSuites)
        assertEquals(emptyList<String>(), redisOptions.netClientOptions.crlPaths)
        assertEquals(NetClientOptions.DEFAULT_ENABLED_SECURE_TRANSPORT_PROTOCOLS, ArrayList(redisOptions.netClientOptions.enabledSecureTransportProtocols))
        assertEquals(NetClientOptions.DEFAULT_TCP_QUICKACK, redisOptions.netClientOptions.isTcpQuickAck)

        assertEquals(listOf(RedisOptions.DEFAULT_ENDPOINT), redisOptions.endpoints)
        assertEquals(RedisRole.MASTER, redisOptions.role)
        assertEquals(RedisSlaves.NEVER, redisOptions.useSlave)
        assertEquals(RedisClientType.STANDALONE, redisOptions.type)
        assertEquals(6, redisOptions.maxPoolSize)
    }
}