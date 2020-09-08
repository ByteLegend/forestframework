package io.forestframework.core.http.routing;


import io.forestframework.core.ForestApplication;
import io.forestframework.core.http.HttpException;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.Router;
import io.forestframework.ext.core.IncludeComponents;
import io.forestframework.testfixtures.DisableAutoScan;
import io.forestframework.testsupport.ForestExtension;
import io.forestframework.testsupport.ForestTest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

@ForestApplication
class MyCustomErrorHandlerApp {
}

@Router("/custom500")
class MyCustom500Handler extends AbstractTraceableRouter {

    @PreHandler(value = "/errorInPreHandler")
    public void preHandleError(HttpServerRequest request, HttpServerResponse response) {
        addToTrace("errorInPreHandler");
        throw new RuntimeException("errorInPreHandler");
    }

    @Route(path = "/errorInHandler", type = RoutingType.HANDLER)
    public void handleException(HttpServerRequest request, HttpServerResponse response) {
        addToTrace("errorInHandler");
        throw new RuntimeException("errorInHandler");
    }

    @OnError(value = "/**")
    public void on500(HttpServerResponse response, Throwable e) {
        response.write(Message.CUSTOM_500_ERROR_HANDLER.name());
        response.write("  e.getMessage():  " + e.getMessage());
        addToTrace(Message.CUSTOM_500_ERROR_HANDLER.name());
    }
}

@Router("/custom4XXerror")
class Custom4XXErrorHandler extends AbstractTraceableRouter {

    @Route(path = "/METHOD_NOT_ALLOWED", type = RoutingType.HANDLER)
    public void handler405(HttpServerResponse response) {
        addToTrace("METHOD_NOT_ALLOWED");
        throw new HttpException(HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Route(path = "/NOT_ACCEPTABLE", type = RoutingType.HANDLER)
    public void handler406(HttpServerResponse response) {
        addToTrace("NOT_ACCEPTABLE");
        throw new HttpException(HttpStatusCode.NOT_ACCEPTABLE);
    }

    @Route(path = "/UNSUPPORTED_MEDIA_TYPE", type = RoutingType.HANDLER)
    public void handler415(HttpServerResponse response) {
        addToTrace("UNSUPPORTED_MEDIA_TYPE");
        throw new HttpException(HttpStatusCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @OnError(value = "/**", statusCode = HttpStatusCode.NOT_FOUND)
    public void on404(HttpServerResponse response, Throwable e) {
        response.write(Message.CUSTOM_404_ERROR_HANDLER.name());
        response.write("  e.getMessage():  " + e.getMessage());
        addToTrace(Message.CUSTOM_404_ERROR_HANDLER.name());
    }

    @OnError(value = "/**", statusCode = HttpStatusCode.METHOD_NOT_ALLOWED)
    public void on405(HttpServerResponse response, Throwable e) {
        response.write(Message.CUSTOM_4XX_ERROR_HANDLER.name());
        response.write("  e.getMessage():  " + e.getMessage());
        addToTrace(Message.CUSTOM_4XX_ERROR_HANDLER.name());
    }

    @OnError(value = "/**", statusCode = HttpStatusCode.NOT_ACCEPTABLE)
    public void on406(HttpServerResponse response, Throwable e) {
        response.write(Message.CUSTOM_4XX_ERROR_HANDLER.name());
        response.write("  e.getMessage():  " + e.getMessage());
        addToTrace(Message.CUSTOM_4XX_ERROR_HANDLER.name());
    }

    @OnError(value = "/**", statusCode = HttpStatusCode.UNSUPPORTED_MEDIA_TYPE)
    public void on415(HttpServerResponse response, Throwable e) {
        response.write(Message.CUSTOM_4XX_ERROR_HANDLER.name());
        response.write("  e.getMessage():  " + e.getMessage());
        addToTrace(Message.CUSTOM_4XX_ERROR_HANDLER.name());
    }
}

@Router("/errorInCustomErrorHandler")
class ErrorInCustomErrorHandler extends AbstractTraceableRouter {

    @Route(value = "/post", type = RoutingType.HANDLER)
    public void handler(HttpServerResponse response) {
        addToTrace(Message.HANDLER.name());
        throw new RuntimeException(Message.ERROR_IN_CUSTOM_ERROR_HANDLER.name());
    }

    @OnError(value = "/**")
    public void errorHandler(HttpServerResponse response, Throwable e) {
        addToTrace(Message.CUSTOM_ERROR_HANDLER.name());
        response.write(Message.CUSTOM_ERROR_HANDLER.name());
        response.write("  e.getMessage():  " + e.getMessage());
        throw new RuntimeException(Message.CUSTOM_ERROR_HANDLER.name());
    }
}

@Router("/errorInPostHandler")
class ErrorInPostHandler extends AbstractTraceableRouter {

    @Get("/**")
    public void handler(HttpServerResponse response) {
        addToTrace(Message.HANDLER.name());
    }

    @OnError(value = "/**")
    public void customErrorHandler(HttpServerResponse response, Throwable e) {
        response.write(" should not be handled by custom error handler. ");
        response.write(" e.getMessage(): " + e.getMessage());
        addToTrace(Message.CUSTOM_ERROR_HANDLER.name());
    }

    @PostHandler("/**")
    public void postHandler(HttpServerResponse response) {
        addToTrace(Message.ERROR_IN_POSTHANDLER.name());
        throw new RuntimeException(Message.ERROR_IN_POSTHANDLER.name());
    }
}

@Router("/uncaught404")
class Uncaught404Error extends AbstractTraceableRouter {
}

@Router("/prehandlerError")
class PrehandlerErrorPreventPropagation extends AbstractTraceableRouter {

    @PreHandler(value = "/**")
    public void preHandler() {
        addToTrace(Message.ERROR_IN_PREHANDLER.name());
        throw new RuntimeException(Message.ERROR_IN_PREHANDLER.name());
    }

    @Route(value = "/**", type = RoutingType.HANDLER)
    public void handler(HttpServerResponse response) {
        addToTrace("shouldNotContinueInHandler");
    }

    @OnError(value = "/**")
    public void customErrorHandler(HttpServerResponse response, Throwable e) {
        response.write(" should be handled by custom error handler. ");
        response.write(" e.getMessage(): " + e.getMessage());
        addToTrace("shouldBeHandledByCustomErrorHandler");
    }

    @PostHandler("/**")
    public void postHandler() {
        addToTrace("shouldContinueInPostHandler");
    }
}

@ExtendWith(ForestExtension.class)
@ForestTest(appClass = MyCustomErrorHandlerApp.class)
@DisableAutoScan
@IncludeComponents(classes = {
        MyCustom500Handler.class,
        Custom4XXErrorHandler.class,
        ErrorInCustomErrorHandler.class,
        ErrorInPostHandler.class,
        Uncaught404Error.class,
        PrehandlerErrorPreventPropagation.class,
})
public class HandlerIntegrationTest extends AbstractMultipleRoutersIntegrationTest {

    @Inject
    void setRouters(MyCustom500Handler myCustom500Handler,
                    Custom4XXErrorHandler custom4XXErrorHandler,
                    ErrorInCustomErrorHandler errorInCustomErrorHandler,
                    ErrorInPostHandler errorInPostHandler,
                    Uncaught404Error uncaught404Error,
                    PrehandlerErrorPreventPropagation prehandlerErrorPreventPropagation) {

        this.addToRouters(myCustom500Handler,
                custom4XXErrorHandler,
                errorInCustomErrorHandler,
                errorInPostHandler,
                uncaught404Error,
                prehandlerErrorPreventPropagation);
    }

    @ParameterizedTest
    @CsvSource({
            "errorInPreHandler",
            "errorInHandler"
    })
    void exceptionsInPrehandlerAndHandlerAreCapturedByCustom500Handler(String handler) throws IOException {
        String path = "/custom500/" + handler;
        String result = sendHttpRequest("GET", path).assert200().getBody();

        Assertions.assertEquals(Arrays.asList(handler, Message.CUSTOM_500_ERROR_HANDLER.name()), this.getTraces());
        assertThat(result, containsString(handler));
        assertThat(result, containsString(Message.CUSTOM_500_ERROR_HANDLER.name()));

        assertThat(result, not(containsString("Resource not found")));
    }

    @Test
    void error404IsHandledByCustom404ErrorHandler() throws IOException {
        String path = "/custom4XXerror/inexistence";
        String result = sendHttpRequest("GET", path).assert200().getBody();

        Assertions.assertEquals(Collections.singletonList(Message.CUSTOM_404_ERROR_HANDLER.name()), getTraces());
        assertThat(result, containsString(path));
        assertThat(result, containsString(Message.CUSTOM_404_ERROR_HANDLER.name()));

        assertThat(result, not(containsString("Resource not found")));

        // this tests `Fix special case when invokeFinalizingHandler with null`
        assertThat(result, not(containsString("INTERNAL_SERVER_ERROR")));
    }

    @ParameterizedTest
    @CsvSource({
            "NOT_ACCEPTABLE",
            "METHOD_NOT_ALLOWED",
            "UNSUPPORTED_MEDIA_TYPE"
    })
    void error4XXIsHandledByCustom4XXErrorHandler(String handler) throws IOException {
        String path = "/custom4XXerror/" + handler;
        String result = sendHttpRequest("GET", path).getBody();

        Assertions.assertEquals(Collections.synchronizedList(Arrays.asList(handler, Message.CUSTOM_4XX_ERROR_HANDLER.name())), getTraces());
        assertThat(result, containsString(handler));
        assertThat(result, containsString(Message.CUSTOM_4XX_ERROR_HANDLER.name()));

        assertThat(result, not(containsString("Resource not found")));
    }

    @Test
    void exceptionsInCustomErrorHandlersAreHandledByFallbackErrorHandler() throws IOException {
        String result = sendHttpRequest("POST", "/errorInCustomErrorHandler/post").getBody();

        Assertions.assertEquals(Collections.synchronizedList(Arrays.asList(Message.HANDLER.name(), Message.CUSTOM_ERROR_HANDLER.name())), getTraces());
        assertThat(result, containsString(Message.ERROR_IN_CUSTOM_ERROR_HANDLER.name()));

        assertThat(result, not(containsString("Resource not found")));
    }

    @Test
    void exceptionsInPostHandlersAreHandledByFallbackErrorHandlerNotCustomHandler() throws IOException {
        String result = sendHttpRequest("GET", "/errorInPostHandler/random").getBody();

        assertThat(getTraces(), not(hasItem(Message.CUSTOM_ERROR_HANDLER.name())));
        Assertions.assertEquals(
                Collections.synchronizedList(Arrays.asList(
                        Message.HANDLER.name(),
                        Message.ERROR_IN_POSTHANDLER.name()
                )), getTraces());

        assertThat(result, not(containsString("Resource not found")));
    }

    @Test
    void uncaught404ErrorIsHandledByFallbackErrorHandler() throws IOException {
        String result = sendHttpRequest("GET", "/uncaught404/inexistence").getBody();

        assertThat(getTraces(), not(hasItem("uncaught404Error")));
        assertThat(getTraces(), not(hasItem(Message.CUSTOM_ERROR_HANDLER.name())));
        Assertions.assertEquals(HttpStatusCode.NOT_FOUND.name(), result);

        assertThat(result, not(containsString("Resource not found")));
    }

    @Test
    void httpExceptionCanBeThrownFromPreHandlersToPreventPropagation() throws IOException {
        String result = sendHttpRequest("GET", "/prehandlerError/random").getBody();

        Assertions.assertEquals(
                Arrays.asList(
                        Message.ERROR_IN_PREHANDLER.name(),
                        "shouldBeHandledByCustomErrorHandler",
                        "shouldContinueInPostHandler"
                ), getTraces());
        assertThat(result, containsString(Message.ERROR_IN_PREHANDLER.name()));
        assertThat(result, containsString("should be handled by custom error handler"));

        assertThat(result, not(containsString("Resource not found")));
    }
}
