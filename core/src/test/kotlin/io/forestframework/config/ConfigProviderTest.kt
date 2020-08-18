package io.forestframework.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.forestframework.core.config.ConfigProvider
import io.github.glytching.junit.extension.system.SystemProperties
import io.github.glytching.junit.extension.system.SystemProperty
import io.vertx.core.http.Http2Settings
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetClientOptions
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions
import io.vertx.redis.client.RedisClientType
import io.vertx.redis.client.RedisOptions
import io.vertx.redis.client.RedisRole
import io.vertx.redis.client.RedisSlaves
import java.io.File
import java.util.Collections.emptyList
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@Suppress("UNCHECKED_CAST")
class ConfigProviderTest {
    val yamlParser = ObjectMapper(YAMLFactory())
    val jsonParser = ObjectMapper()

    @ParameterizedTest(name = "{index}")
    @ValueSource(strings = [
        """
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
    """,
        """
{
    "aaa": {
        "bbb": {
        
            "ccc": {
                "stringValue1": "",
                "stringValue2": "This is a string",
                "intValue": 42,
                "listValue": ["a", 42, "c" ]
            }
        }
    }
}
        """])
    fun `can read raw property`(configString: String) {
        val parser = if (configString.trim().startsWith("{")) jsonParser else yamlParser
        val provider = ConfigProvider(parser.readValue(configString, Map::class.java) as MutableMap<String, Any>, emptyMap())
        assertEquals("", provider.getInstance("aaa.bbb.ccc.stringValue1", String::class.java))
        assertEquals("", provider.getInstance("aaa.bbb.ccc", JsonObject::class.java).getString("stringValue1"))
        assertEquals("This is a string", provider.getInstance("aaa.bbb.ccc.stringValue2", String::class.java))
        assertEquals(42, provider.getInstance("aaa.bbb.ccc.intValue", Integer::class.java))
        assertEquals(42, provider.getInstance("aaa.bbb.ccc", JsonObject::class.java).getInteger("intValue"))
        assertEquals(null, provider.getInstance("aaa.notexist", Integer::class.java))
        assertEquals(null, provider.getInstance("aaa.notexist.notexist", Boolean::class.java))
        assertEquals(null, provider.getInstance("aaa.notexist.notexist", HttpServerOptions::class.java))
    }

    val realWorldConfig = """
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

    @Test
    fun `can read Options property`() {
        val provider = ConfigProvider(yamlParser.readValue(realWorldConfig, Map::class.java) as MutableMap<String, Any>, emptyMap())

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

        provider.addDefaultOptions("forest.redis", { RedisOptions() })

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
        val provider = ConfigProvider(emptyMap(), emptyMap())

        val httpServerOptions: HttpServerOptions = provider.getInstance("forest.http", HttpServerOptions::class.java)
        assertEquals(HttpServerOptions.DEFAULT_PORT, httpServerOptions.port)
        assertEquals(HttpServerOptions.DEFAULT_COMPRESSION_SUPPORTED, httpServerOptions.isCompressionSupported)
        assertEquals(Http2Settings.DEFAULT_ENABLE_PUSH, httpServerOptions.initialSettings.isPushEnabled)
        assertEquals(Http2Settings.DEFAULT_MAX_HEADER_LIST_SIZE.toLong(), httpServerOptions.initialSettings.maxHeaderListSize)
        assertEquals(null, httpServerOptions.initialSettings.extraSettings)

        provider.addDefaultOptions("forest.redis", { RedisOptions() })
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

    // https://youtrack.jetbrains.com/issue/KT-12794
    @Test
    @SystemProperties(
        SystemProperty(name = "forest.http.port", value = "12345"),
        SystemProperty(name = "forest.http.initialSettings.headerTableSize", value = "8192")
    )
    fun `environment config overwrites default value`() {
        val provider = ConfigProvider.load()

        val httpServerOptions: HttpServerOptions = provider.getInstance("forest.http", HttpServerOptions::class.java)
        assertEquals(12345, httpServerOptions.port)
        assertEquals(8192, httpServerOptions.initialSettings.headerTableSize)
        assertEquals(HttpServerOptions.DEFAULT_COMPRESSION_SUPPORTED, httpServerOptions.isCompressionSupported)
        assertEquals(Http2Settings.DEFAULT_ENABLE_PUSH, httpServerOptions.initialSettings.isPushEnabled)

        val initialSettings: Http2Settings = provider.getInstance("forest.http.initialSettings", Http2Settings::class.java)
        assertEquals(8192, initialSettings.headerTableSize)
    }

    @Test
    @SystemProperties(
        SystemProperty(name = "forest.http.port", value = "12345"),
        SystemProperty(name = "forest.http.initialSettings.headerTableSize", value = "8192")
    )
    fun `environment config overwrites file config`(@TempDir tempDir: File) {
        withSystemPropertyConfigFile(tempDir, realWorldConfig) {
            val provider = ConfigProvider.load()

            val httpServerOptions: HttpServerOptions = provider.getInstance("forest.http", HttpServerOptions::class.java)
            assertEquals(12345, httpServerOptions.port)
            assertEquals(8192, httpServerOptions.initialSettings.headerTableSize)
            assertEquals(true, httpServerOptions.isCompressionSupported)
            assertEquals(false, httpServerOptions.initialSettings.isPushEnabled)

            val initialSettings: Http2Settings = provider.getInstance("forest.http.initialSettings", Http2Settings::class.java)
            assertEquals(8192, initialSettings.headerTableSize)

            assertEquals(8192, provider.getInstance("forest.http.initialSettings.headerTableSize", Integer::class.java))
        }
    }

    @Test
    @SystemProperty(name = "forest.http", value = """
        {
            "port": 12345,
            "initialSettings": {
                 "headerTableSize": 8192
            }
        }
        """)
    fun `environment json config overwrites file config`(@TempDir tempDir: File) {
        withSystemPropertyConfigFile(tempDir, realWorldConfig) {
            val provider = ConfigProvider.load()

            val httpServerOptions: HttpServerOptions = provider.getInstance("forest.http", HttpServerOptions::class.java)
            assertEquals(12345, httpServerOptions.port)
            assertEquals(8192, httpServerOptions.initialSettings.headerTableSize)
            assertEquals(true, httpServerOptions.isCompressionSupported)
            assertEquals(false, httpServerOptions.initialSettings.isPushEnabled)

            val initialSettings: Http2Settings = provider.getInstance("forest.http.initialSettings", Http2Settings::class.java)
            assertEquals(8192, initialSettings.headerTableSize)

            assertEquals(8192, provider.getInstance("forest.http.initialSettings.headerTableSize", Integer::class.java))
        }
    }

    @Test
    @SystemProperty(name = "forest.redis.endpoints", value = "[\"redis://localhost:6380\"]")
    fun `can parse JSON array`() {
        val provider = ConfigProvider.load()
        provider.addDefaultOptions("forest.redis") { RedisOptions() }

        assertEquals(listOf("redis://localhost:6380"), provider.getInstance("forest.redis.endpoints", List::class.java))
        assertEquals(listOf("redis://localhost:6380"), provider.getInstance("forest.redis", RedisOptions::class.java).endpoints)
    }

    @Test
    fun `can add to config`(@TempDir tempDir: File) {
        withSystemPropertyConfigFile(tempDir, realWorldConfig) {
            val provider = ConfigProvider.load()
            provider.addConfig("forest.http.port", "12345")
            provider.addConfig("forest.http.initialSettings.headerTableSize", "8192")

            val httpServerOptions: HttpServerOptions = provider.getInstance("forest.http", HttpServerOptions::class.java)
            assertEquals(12345, httpServerOptions.port)
            assertEquals(8192, httpServerOptions.initialSettings.headerTableSize)
            assertEquals(true, httpServerOptions.isCompressionSupported)
            assertEquals(false, httpServerOptions.initialSettings.isPushEnabled)

            val initialSettings: Http2Settings = provider.getInstance("forest.http.initialSettings", Http2Settings::class.java)
            assertEquals(8192, initialSettings.headerTableSize)

            assertEquals(8192, provider.getInstance("forest.http.initialSettings.headerTableSize", Integer::class.java))
        }
    }

    @Test
    @SystemProperty(name = "forest.bridge", value = """
        {
            "outboundPermitteds": [
              {
                 "addressRegex": "auction\\.[0-9]+" 
              }
            ] 
        }
        """)
    fun `can configure option list`() {
        val provider = ConfigProvider.load()

        provider.addDefaultOptions("forest.bridge", ::SockJSBridgeOptions)
        val options = provider.getInstance("forest.bridge", SockJSBridgeOptions::class.java)
        assertEquals(listOf("auction\\.[0-9]+"), options.outboundPermitteds.map { it.addressRegex })
    }

    private fun withSystemPropertyConfigFile(tempDir: File, fileContent: String, function: () -> Unit) {
        val originalConfig = System.getProperty("forest.config.file")
        val configFile = File(tempDir, "config.yml").apply { writeText(fileContent) }
        System.setProperty("forest.config.file", configFile.absolutePath)
        try {
            function()
        } finally {
            if (originalConfig != null) {
                System.setProperty("forest.config.file", originalConfig)
            } else {
                System.clearProperty("forest.config.file")
            }
        }
    }
}
