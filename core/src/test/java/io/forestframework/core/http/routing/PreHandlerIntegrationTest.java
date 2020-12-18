package io.forestframework.core.http.routing;

import io.forestframework.core.ForestApplication;
import io.forestframework.core.http.HttpRequest;
import io.forestframework.core.http.HttpResponse;
import io.forestframework.ext.core.IncludeComponents;
import io.forestframework.testfixtures.DisableAutoScan;
import io.forestframework.testsupport.ForestExtension;
import io.forestframework.testsupport.ForestIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

@ForestApplication
class PreHandlerTestApp extends AbstractTraceableRouter {

    @PreHandler(value = "/prehandler/*")
    public void preHandle1(HttpRequest request, HttpResponse response) {
        response.writeLater(request.path());
        addToTrace(request.path());
    }

    @PreHandler("/prehandler/error/*")
    public boolean preHandleException(HttpRequest request, HttpResponse response) {
        response.setStatusCode(503).writeLater("Service Unavailable for " + request.path());
        addToTrace(request.path());
        return false;
    }

    @Route(path = "/prehandler/*", type = RoutingType.HANDLER)
    public void handle(HttpResponse response) {
        response.writeLater(" is handled");
        addToTrace(Message.HANDLER.name());
    }

    @Route(path = "/prehandler/error/*", type = RoutingType.HANDLER)
    public void handleException(HttpResponse response) {
        response.writeLater(" should not be here");
        addToTrace(Message.HANDLER.name());
    }
}

@DisplayName("When pre-handler methods default")
@ExtendWith(ForestExtension.class)
@ForestIntegrationTest(appClass = PreHandlerTestApp.class)
@DisableAutoScan
@IncludeComponents(classes = {PreHandlerTestApp.class})
public class PreHandlerIntegrationTest extends AbstractHandlerIntegrationTest {

    @Inject
    void setRouter(PreHandlerTestApp router) {
        this.router = router;
    }

    @ParameterizedTest(name = "should handle \"{0}\" method and continue")
    @CsvSource({"GET", "POST", "PATCH", "DELETE"})
    void shouldHandleAllHttpMethodsWhenMethodsAttributeIsDefaultWhenPreHandlerReturnsVoid(String method) throws IOException {
        String path = "/prehandler/" + method.toLowerCase();
        String expected = "/prehandler/" + method.toLowerCase() + " is handled";

        String result = sendHttpRequest(method, path).assert200().getBody();

        Assertions.assertEquals(expected, result);
        Assertions.assertEquals(Collections.synchronizedList(Arrays.asList(path, Message.HANDLER.name())), router.traces);
    }

    @ParameterizedTest(name = "should handle \"{0}\" and return")
    @CsvSource({"GET", "POST", "PATCH", "DELETE"})
    void shouldHandleAllHttpMethodsWhenMethodsAttributeIsDefaultWhenPreHandlerReturnsFalse(String method) throws IOException {
        String path = "/prehandler/error/" + method.toLowerCase();
        String expected = "Service Unavailable for /prehandler/error/" + method.toLowerCase();

        String result = sendHttpRequest(method, path).assert503().getBody();

        Assertions.assertEquals(expected, result);
        assertThat(result, not(containsString("should not be here")));
        Assertions.assertEquals(Collections.singletonList(path), router.traces);
        assertThat(router.traces, not(hasItem(Message.HANDLER.name())));
    }
}
