package io.forestframework.core.http;

import io.forestframework.core.injector.DefaultImplementedBy;
import io.vertx.core.http.HttpServerRequest;
import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL, since = "0.1")
public interface RequestHandlerChain {
    void handleNext(HttpServerRequest request);

    //    private final List<WebRequestHandler> handlers;
//
//    @Inject
//    public RequestHandlerChain(Http404RequestHandler http404RequestHandler,
//                               FastRequestHandler fastRequestHandler,
//                               RouterRequestHandler routerRequestHandler) {
//        this.handlers = Arrays.asList(http404RequestHandler, fastRequestHandler, routerRequestHandler);
//        http404RequestHandler.setNext(fastRequestHandler);
//        fastRequestHandler.setNext(routerRequestHandler);
//    }
//
//    @Override
//    public void handle(HttpServerRequest request) {
//        handlers.get(0).handle(request);
//    }
}
