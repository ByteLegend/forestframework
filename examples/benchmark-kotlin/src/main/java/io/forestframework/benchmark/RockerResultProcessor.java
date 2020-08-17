package io.forestframework.benchmark;

import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.RockerOutputFactory;
import generated.FortunesTemplate;
import io.forestframework.benchmark.model.Fortune;
import io.forestframework.core.http.PlainHttpContext;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.result.RoutingResultProcessor;
import io.forestframework.core.http.routing.Routing;
import io.vertx.core.http.HttpHeaders;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class RockerResultProcessor implements RoutingResultProcessor {
    private static final CharSequence HEADER_CONTENT_TYPE = HttpHeaders.createOptimized("content-type");
    private static final CharSequence RESPONSE_TYPE_HTML = HttpHeaders.createOptimized("text/html; charset=UTF-8");
    private final RockerOutputFactory<BufferRockerOutput> factory = BufferRockerOutput.factory(ContentType.RAW);

    @SuppressWarnings("unchecked")
    @Override
    public Object processResponse(WebContext context, Routing routing, Object returnValue) {
        List<Fortune> fortunes = (List<Fortune>) returnValue;
        ((PlainHttpContext) context).response().putHeader(HEADER_CONTENT_TYPE, RESPONSE_TYPE_HTML)
                .end(FortunesTemplate.template(fortunes).render(factory).buffer());
        return returnValue;
    }
}
