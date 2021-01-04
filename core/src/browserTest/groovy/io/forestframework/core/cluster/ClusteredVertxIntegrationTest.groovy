package io.forestframework.core.cluster


import geb.Browser
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.bridge.Bridge
import io.forestframework.core.http.bridge.BridgeEvent
import io.forestframework.core.http.result.GetPlainText
import io.forestframework.ext.api.ConsumeEvent
import io.forestframework.ext.api.DefaultApplicationContext
import io.forestframework.ext.api.WithExtensions
import io.forestframework.ext.core.AutoEventConsumerScanExtension
import io.forestframework.ext.core.AutoStaticResourceScan
import io.forestframework.testfixtures.HttpClient
import io.forestframework.testsupport.utils.FreePortFinder
import io.vertx.core.eventbus.EventBus
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.openqa.selenium.chrome.ChromeOptions
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

import javax.inject.Inject

import static io.forestframework.testfixtures.TestUtilsKt.withSystemPropertyConfigFile
import static org.testcontainers.Testcontainers.exposeHostPorts

/*
 - Start ZooKeeper docker container.
 - Start App1 and App2 with zookeeper cluster manager
 - Client1 sends a bridge message to App1
   - App1/App2 gets the message.
 - Client1 (browser) listens bridge message from App1
   - App2 sends a message via distributed event bus
   - Verify Client1 gets the message.
 */

class ClusteredVertxIntegrationTest {
    static GenericContainer zookeeper = new GenericContainer(DockerImageName.parse("zookeeper:3.6.2"))
        .withExposedPorts(2181)
    private static BrowserWebDriverContainer browserWebDriverContainer
    static DefaultApplicationContext app1
    static DefaultApplicationContext app2

    static int app1HttpPort
    static int app2HttpPort
    static int app1EventBusPort
    static int app2EventBusPort

    @BeforeAll
    static void setUp(@TempDir File tempDir) {
        zookeeper.start()
        app1HttpPort = FreePortFinder.findFreeLocalPort()
        app2HttpPort = FreePortFinder.findFreeLocalPort()
        app1EventBusPort = FreePortFinder.findFreeLocalPort()
        app2EventBusPort = FreePortFinder.findFreeLocalPort()

        exposeHostPorts(app1HttpPort, app2HttpPort)

        app1 = withSystemPropertyConfigFile(tempDir, createConfigFile(app1HttpPort, app1EventBusPort)) {
            (DefaultApplicationContext) Forest.run(ClusteredVertxIntegrationTestApp.class)
        }
        app2 = withSystemPropertyConfigFile(tempDir, createConfigFile(app2HttpPort, app2EventBusPort)) {
            (DefaultApplicationContext) Forest.run(ClusteredVertxIntegrationTestApp.class)
        }

        browserWebDriverContainer = new BrowserWebDriverContainer().with {
            withCapabilities(new ChromeOptions())
            withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, null)
            it
        }
        browserWebDriverContainer.start()
    }

    @AfterAll
    static void tearDown() {
        app1?.close()
        app2?.close()
        browserWebDriverContainer?.stop()
        zookeeper?.stop()
    }

    @Test
    void "all servers get event from client"() {
        ClusteredVertxIntegrationTestApp testApp1 = app1.injector.getInstance(ClusteredVertxIntegrationTestApp)
        ClusteredVertxIntegrationTestApp testApp2 = app2.injector.getInstance(ClusteredVertxIntegrationTestApp)
        Browser.drive(driver: browserWebDriverContainer.webDriver) {
            go "http://host.testcontainers.internal:${app1HttpPort}/index.html"
            Thread.sleep(1000)
            $("#sendClientEventButton").click()
            waitFor(5) {
                testApp1.receivedClientEvents == ["This is a client-side event"] as Set &&
                    testApp2.receivedClientEvents == ["This is a client-side event"] as Set
            }
        }
    }

    @Test
    void "client gets event from clustered event bus"() {
        Browser.drive(driver: browserWebDriverContainer.webDriver) {
            go "http://host.testcontainers.internal:${app1HttpPort}/index.html"
            Thread.sleep(1000)
            HttpClient.@Companion.create().get(app2HttpPort, "/sendServerEvent", [:]).assert200().bodyAsString()

            waitFor(5) {
                $(".message").size() == 1 && $(".message")[0].text().contains("This is a server-side event")
            }
        }
    }

    private static String createConfigFile(int httpPort, int eventBusPort) {
        return """
http:
  port: $httpPort
vertx:
  clusterManager:
    type: io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager
    zookeeperHosts: "localhost:${zookeeper.getFirstMappedPort()}"
    sessionTimeout: 20000
    connectTimeout: 3000
    rootPath: "io.vertx"
    retry:
      initialSleepTime: 100
      intervalTimes: 10000
      maxTimes: 5
  eventBusOptions:
    clusterPublicHost: localhost
    clusterPublicPort: $eventBusPort
    host: localhost
    port: $eventBusPort
bridge:
  /eventbus:
    inboundPermitteds:
    - addressRegex: ".*"
    outboundPermitteds:
    - addressRegex: ".*"
"""
    }
}

@AutoStaticResourceScan(webroot = "ClusteredVertxIntegrationTestData")
@ForestApplication
@WithExtensions(extensions = [AutoEventConsumerScanExtension.class])
class ClusteredVertxIntegrationTestApp {
    @Inject
    EventBus eventBus

    Set<String> receivedClientEvents = []

    @ConsumeEvent("client.event")
    void onClientEvent(String message) {
        receivedClientEvents.add(message)
    }

    @GetPlainText("/sendServerEvent")
    String sendServerEvent() {
        eventBus.publish("server.event", "This is a server-side event")
        return "OK"
    }

    @Bridge("/eventbus")
    void bridgeEvent(BridgeEvent event) {
        event.complete(true)
    }
}

//@After(classes = [AutoComponentScanExtension.class])
//class AutoEventBusHandlerScanner implements Extension {
//    @Override
//    void configure(Injector injector) {
//        ApplicationContext ac = injector.getInstance(ApplicationContext.class)
//        ac.getComponents().each { registerEventConsumers(injector, ac.getVertx(), it) }
//    }
//
//    private static void registerEventConsumers(Injector injector, Vertx vertx, Class<?> component) {
//        component.methods.each { method ->
//            EventConsumer anno = method.getAnnotation(EventConsumer.class)
//            if (anno != null) {
//                vertx.eventBus().consumer(anno.value()) {
//                    method.invoke(injector.getInstance(method.getDeclaringClass()), it.body())
//                }
//            }
//        }
//    }
//}
//
//@Retention(RetentionPolicy.RUNTIME)
//@interface EventConsumer {
//    String value()
//}
