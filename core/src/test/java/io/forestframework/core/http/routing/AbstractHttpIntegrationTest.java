package io.forestframework.core.http.routing;

import com.google.common.collect.ImmutableMap;
import io.forestframework.core.http.HttpMethod;
import io.forestframework.testfixtures.AbstractForestIntegrationTest;
import io.forestframework.testfixtures.HttpClientResponse;
import org.junit.jupiter.api.Assertions;

import static java.util.Collections.emptyMap;

public class AbstractHttpIntegrationTest extends AbstractForestIntegrationTest {
    public HttpResponse sendHttpRequest(String method, String path) {
        HttpClientResponse response = send(HttpMethod.valueOf(method), path,
                                           ImmutableMap.of("Accept", "application/json",
                                                           "Content-Type", "application/json"),
                                           "",
                                           emptyMap());
        return new HttpResponse(response.getStatusCode(), response.bodyAsString());
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

        HttpResponse assert405() {
            return assertStatusCode(405);
        }

        HttpResponse assert406() {
            return assertStatusCode(406);
        }

        HttpResponse assert500() {
            return assertStatusCode(500);
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
