package io.forestframework.core.http.routing;

import io.forestframework.core.ForestApplication;
import io.forestframework.core.config.Config;
import io.forestframework.testfixtures.DisableAutoScan;
import io.forestframework.testsupport.ForestExtension;
import io.forestframework.testsupport.ForestTest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.inject.Inject;
import java.io.IOException;


@ForestApplication
class PreHandlerTestApp {

    @PreHandler(value = "/prehandler/*")
    public void preHandle(HttpServerRequest request, HttpServerResponse response) {
        response.write(request.path(), "UTF-8");
    }

    @PreHandler("/prehandler/error/*")
    public boolean preHandleException(HttpServerRequest request, HttpServerResponse response) {
        response.setStatusCode(503).write("Service Unavailable for " + request.path());
        return false;
    }

    @Route(path = "/prehandler/*", type = RoutingType.HANDLER)
    public void handle(HttpServerResponse response) {
        response.write(" is handled", "UTF-8");
    }

    @Route(path = "/prehandler/error/*", type = RoutingType.HANDLER)
    public void handleException(HttpServerResponse response) {
        response.write(" should not be here", "UTF-8");
    }
}

@DisplayName("When pre-handler methods default")
@ExtendWith(ForestExtension.class)
@ForestTest(appClass = PreHandlerTestApp.class)
@DisableAutoScan
public class PreHandlerIntegrationTest {

    @Inject
    @Config("forest.http.port")
    Integer port;

    CloseableHttpClient client;

    @BeforeEach
    void setUp() {
        client = HttpClients.createDefault();
    }

    @ParameterizedTest(name = "should handle \"{0}\" method")
    @CsvSource({"GET", "POST", "PATCH", "DELETE"})
    void should_handle_all_methods_when_methods_attribute_is_default_value_in_preHandler(String method) throws IOException {
        String path = "/prehandler/" + method.toLowerCase();
        String expected = "/prehandler/" + method.toLowerCase() + " is handled";

        CloseableHttpResponse response = sendRequestGetResponse(method, path);
        int statusCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());

        Assertions.assertEquals(200, statusCode);
        Assertions.assertEquals(expected, result);
    }

    @ParameterizedTest(name = "should handle \"{0}\" and return")
    @CsvSource({"GET", "POST", "PATCH", "DELETE"})
    void should_handle_all_methods_when_methods_attribute_is_default_value_in_preHandler_and_return_when_error_occurs(String method) throws IOException {
        String path = "/prehandler/error/" + method.toLowerCase();
        String expected = "Service Unavailable for /prehandler/error/" + method.toLowerCase();

        CloseableHttpResponse response = sendRequestGetResponse(method, path);
        int statusCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());

        Assertions.assertEquals(503, statusCode);
        Assertions.assertEquals(expected, result);
        Assertions.assertFalse(result.contains("should not be here"));
    }

    private CloseableHttpResponse sendRequestGetResponse(String method, String path) throws IOException {
        String uri = "http://localhost:" + port + path;
        HttpUriRequest request = RequestBuilder.create(method).setUri(uri).build();
        return client.execute(request);
    }
}
