package io.forestframework.util.completablefuture;

import io.forestframework.testsupport.utils.FreePortFinder;
import io.forestframework.utils.completablefuture.VertxCompletableFuture;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.condition.OS;
import org.junit.runner.RunWith;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class HttpClientTest {

  private Vertx vertx;
  private int port = FreePortFinder.findFreeLocalPort();

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();

    vertx.createHttpServer().requestHandler(request -> {
          switch (request.path()) {
            case "/A":
              request.response().end("42");
              break;
            case "/B":
              request.response().end("23");
              break;
            default:
              request.response().end("Hello");
          }
        }
    ).listen(port, tc.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void test(TestContext tc) {
    Assume.assumeTrue(OS.LINUX.isCurrentOs()); // for some reason this test is quite flaky on mac/windows
    Async async = tc.async();

    HttpClientOptions options = new HttpClientOptions().setDefaultPort(port).setDefaultHost("localhost");
    HttpClient client1 = vertx.createHttpClient(options);
    HttpClient client2 = vertx.createHttpClient(options);

    VertxCompletableFuture<Integer> requestA = new VertxCompletableFuture<>(vertx);
    client1.request(HttpMethod.GET, "/A").compose(HttpClientRequest::send).onComplete(asyncResult -> {
        if (asyncResult.succeeded()) {
            asyncResult.result().bodyHandler(buffer -> requestA.complete(Integer.parseInt(buffer.toString())))
                    .exceptionHandler(requestA::completeExceptionally);
        } else {
            requestA.completeExceptionally(asyncResult.cause());
        }
    });

    VertxCompletableFuture<Integer> requestB = new VertxCompletableFuture<>(vertx);
    client2.request(HttpMethod.GET, "/B").compose(HttpClientRequest::send).onComplete(asyncResult -> {
        if (asyncResult.succeeded()) {
            asyncResult.result().bodyHandler(buffer -> requestB.complete(Integer.parseInt(buffer.toString())))
                    .exceptionHandler(requestB::completeExceptionally);
        } else {
            requestB.completeExceptionally(asyncResult.cause());
        }
    });

    VertxCompletableFuture.allOf(requestA, requestB).thenApply(v -> requestA.join() + requestB.join())
        .thenAccept(i -> {
          tc.assertEquals(65, i);
          async.complete();
        });
  }
}
