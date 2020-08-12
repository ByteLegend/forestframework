import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CountDownLatch;

/**
 * This is used by SockJSIntegrationTest for testing `onError` handlers.
 *
 * It will be started by Java 11 scripting engine then killed forcibly to test error handling.
 *
 * It must only use JDK libraries.
 */
public class WebSocketIntegrationTestClient {
    private static final String port = System.getProperty("server.port");
    private static final String username = System.getProperty("user.name");

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        WebSocket ws = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:" + port + "/chat/" + username), new WebSocket.Listener() {
                }).join();
        ws.sendText("Hello from " + username, false);
        new CountDownLatch(1).await();
    }
}
