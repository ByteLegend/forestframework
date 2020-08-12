package io.forestframework.core.http.param;

import com.google.common.collect.ImmutableMap;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class ContentTypeNegotiatingRequestBodyParser implements ContentTypeAwareRoutingParameterResolver<Object> {
    private final Map<String, ContentTypeAwareRoutingParameterResolver<?>> requestBodyParsers;

    @Inject
    public ContentTypeNegotiatingRequestBodyParser(JsonRequestBodyParser jsonRequestBodyParser) {
        this.requestBodyParsers = ImmutableMap.of(jsonRequestBodyParser.contentType(), jsonRequestBodyParser);
    }
//
//    @Override
//    public Object resolveArgument(Routing routing, Context context, int paramIndex) {
//        String contentType = context.parsedHeaders().contentType().value();
//        ContentTypeAwareRoutingParameterResolver<?> parser = requestBodyParsers.get(contentType);
//        if (parser == null) {
//            throw new UnsupportedOperationException("Can't find parser to process " + contentType);
//        }
//        return parser.resolveParameter(routing, context, paramIndex);
//    }

    @Override
    public String contentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object resolveParameter(WebContext context, Routing routing, int paramIndex) {
        return null;
    }
}
