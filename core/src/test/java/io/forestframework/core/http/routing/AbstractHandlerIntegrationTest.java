package io.forestframework.core.http.routing;

import io.forestframework.core.config.Config;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;
import java.io.IOException;

public class AbstractHandlerIntegrationTest {
    protected AbstractTraceableRouter router;

    CloseableHttpClient client;

    @Inject
    @Config("forest.http.port")
    Integer port;

    @BeforeEach
    void setUp() {
        client = HttpClients.createDefault();
        router.traces.clear();
    }

    @AfterEach
    void cleanUp() throws IOException {
        client.close();
    }

    public HttpResponse sendHttpRequest(String method, String path) throws IOException {
        String uri = "http://localhost:" + port + path;
        HttpUriRequest request = RequestBuilder.create(method).setUri(uri).build();
        CloseableHttpResponse response = client.execute(request);
        return new HttpResponse(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity()));
    }

    static class HttpResponse {
        private final int statusCode;
        private final String body;

        public HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        HttpResponse assert200() {
            return assertStatusCode(200);
        }
        HttpResponse assert404() {
            return assertStatusCode(404);
        }

        HttpResponse assert503() {
            return assertStatusCode(503);
        }

        HttpResponse assertStatusCode(int expectedStatusCode) {
            Assertions.assertEquals(expectedStatusCode, statusCode);
            return this;
        }

        public String getBody() {
            return body;
        }
    }
}
