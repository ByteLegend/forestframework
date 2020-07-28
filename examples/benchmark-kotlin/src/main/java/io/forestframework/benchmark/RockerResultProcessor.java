package io.forestframework.benchmark;

import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.RockerOutputFactory;
import generated.FortunesTemplate;
import io.forestframework.core.http.result.RoutingResultProcessor;
import io.forestframework.benchmark.model.Fortune;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class RockerResultProcessor implements RoutingResultProcessor {
    private static final CharSequence HEADER_CONTENT_TYPE = HttpHeaders.createOptimized("content-type");
    private static final CharSequence RESPONSE_TYPE_HTML = HttpHeaders.createOptimized("text/html; charset=UTF-8");
    private final RockerOutputFactory<BufferRockerOutput> factory = BufferRockerOutput.factory(ContentType.RAW);

    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
        List<Fortune> fortunes = (List<Fortune>) returnValue;
        routingContext.response().putHeader(HEADER_CONTENT_TYPE, RESPONSE_TYPE_HTML)
                .end(FortunesTemplate.template(fortunes).render(factory).buffer());
        return returnValue;
    }
}
