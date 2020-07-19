package io.forestframework.core.http.staticresource;

import io.forestframework.core.http.AbstractRoutingContextDecorator;
import io.forestframework.core.http.HttpStatusCode;
import io.forestframework.core.http.result.RoutingResultProcessor;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Singleton;

/**
 * Convert the resource name returned by handler method to a file, then send it via {@link io.vertx.core.http.HttpServerResponse}.
 * The returned resource name MUST be a relative path, which will be interpreted as path relative to classpath entry.
 *
 * Given a resource name "relative/path/to/resource.txt", for directory classpath entry "file:///home/my/resources",
 * the resource file "file:///home/my/resources/relative/path/to/resource.txt" will be located and sent; for jar classpath entry
 * "file:///home/lib/my.jar", the "relative/path/to/resource.txt" inside that jar will be located and sent.
 */
@Singleton
public class StaticResourceProcessor implements RoutingResultProcessor {
    private final ForkedStaticHandlerImpl staticHandler = new ForkedStaticHandlerImpl();

    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
        String path = (String) returnValue;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        EndAwareRoutingContextDecorator endAwareRoutingContext = new EndAwareRoutingContextDecorator(routingContext);
        staticHandler.sendStatic(endAwareRoutingContext, path);
        return endAwareRoutingContext.getFuture();
    }

    public static class EndAwareRoutingContextDecorator extends AbstractRoutingContextDecorator {
        private final Promise<Void> promise = Promise.promise();

        public EndAwareRoutingContextDecorator(RoutingContext delegate) {
            super(delegate);
        }

        public Future<Void> getFuture() {
            return promise.future();
        }

        @Override
        public void next() {
            // Resource not found
            response().setStatusCode(HttpStatusCode.NOT_FOUND.getCode());
            response().end(HttpStatusCode.NOT_FOUND.name());
        }

        @Override
        public Future<Void> end(String chunk) {
            promise.complete();
            return super.end(chunk);
        }

        @Override
        public Future<Void> end(Buffer buffer) {
            promise.complete();
            return super.end(buffer);
        }

        @Override
        public Future<Void> end() {
            promise.complete();
            return super.end();
        }
    }


}

