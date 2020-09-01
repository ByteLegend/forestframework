package io.forestframework.core.http.routing;

import io.forestframework.core.ForestApplication;
import io.forestframework.core.http.Router;
import io.forestframework.ext.core.IncludeComponents;
import io.forestframework.testfixtures.DisableAutoScan;
import io.forestframework.testsupport.ForestExtension;
import io.forestframework.testsupport.ForestTest;
import io.vertx.core.http.HttpServerRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@ForestApplication
class MultipleHandlersApp {
}

@Router("/preHandler")
class PreHandlerReturnsVoidTrueOrFalse extends AbstractTraceableRouter {

    @PreHandler("/returns/*")
    public boolean preHandleReturnsTrueOrFalse(HttpServerRequest request) {
        addToTrace(request.path());
        return request.path().contains("True");
    }

    @PreHandler("/returns/*")
    public void preHandle1ContinuesWhenTrue() {
        addToTrace(Message.PREHANDLER1.name());
    }

    @PreHandler("/returns/*")
    public void preHandle2ContinuesWhenTrue() {
        addToTrace(Message.PREHANDLER2.name());
    }

    @Route(type = RoutingType.PRE_HANDLER, order = 2, path = "/returnsVoid")
    public void preHandle2ContinuesWhenVoid() {
        addToTrace(Message.PREHANDLER2.name());
    }

    @Route(type = RoutingType.PRE_HANDLER, order = 0, path = "/returnsVoid")
    public void preHandlerReturnsVoid(HttpServerRequest request) {
        addToTrace(request.path());
    }

    @Route(type = RoutingType.PRE_HANDLER, order = 1, path = "/returnsVoid")
    public void preHandle1ContinuesWhenVoid() {
        addToTrace(Message.PREHANDLER1.name());
    }

    @Route(path = "/returnsVoid", order = 1, type = RoutingType.HANDLER)
    public void handler2ContinuesWhenPrehandlerReturnsVoid() {
        addToTrace(Message.HANDLER2.name());
    }

    @Route(path = "/returnsVoid", order = 0, type = RoutingType.HANDLER)
    public void handler1ContinuesWhenPrehandlerReturnsVoid() {
        addToTrace(Message.HANDLER1.name());
    }

    @Route(path = "/returns/*", order = 2, type = RoutingType.HANDLER)
    public void handler2ContinuesWhenPrehandlerReturnsTrueOrFalse() {
        addToTrace(Message.HANDLER2.name());
    }

    @Route(path = "/returns/*", order = 1, type = RoutingType.HANDLER)
    public void handler1ContinuesWhenPrehandlerReturnsTrueOrFalse() {
        addToTrace(Message.HANDLER1.name());
    }
}

@ExtendWith(ForestExtension.class)
@ForestTest(appClass = MultipleHandlersApp.class)
@DisableAutoScan
@IncludeComponents(classes = {PreHandlerReturnsVoidTrueOrFalse.class})
public class MultipleHandlersIntegrationTest extends AbstractHandlerIntegrationTest {

    @Inject
    void setRouter(PreHandlerReturnsVoidTrueOrFalse router) {
        this.router = router;
    }

    @ParameterizedTest
    @CsvSource({
            "returns/True",
            "returnsVoid"
    })
    void prehandlersContinueWhenPreviousOnesReturnVoidOrTrueRegardlessOfAppearanceOrder(String handler) throws IOException {
        String path = "/preHandler/" + handler;

        sendHttpRequest("GET", path).assert200();
        Assertions.assertEquals(
                Arrays.asList(
                        path,
                        Message.PREHANDLER1.name(),
                        Message.PREHANDLER2.name(),
                        Message.HANDLER1.name(),
                        Message.HANDLER2.name()
                ), router.traces);
    }

    @ParameterizedTest
    @CsvSource({"returns/False"})
    void prehandlersReturnFalseThenTheFollowingHandlersAreSkipped(String handler) throws IOException {
        String path = "/preHandler/" + handler;

        sendHttpRequest("GET", path).assert200();
        Assertions.assertEquals(Collections.singletonList(path), router.traces);
    }
}
