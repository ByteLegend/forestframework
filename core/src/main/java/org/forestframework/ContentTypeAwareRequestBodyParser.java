package org.forestframework;


import io.vertx.ext.web.RoutingContext;
import org.forestframework.annotation.RequestBody;
import org.forestframework.http.Routing;

public class ContentTypeAwareRequestBodyParser implements RoutingHandlerArgumentResolver<Object, RequestBody> {
    @Override
    public Object resolveArgument(Routing routing, Class<?> argumentType, RoutingContext routingContext, RequestBody annotation) {
        if(argumentType == String.class) {
            return routingContext.getBodyAsString();
        }
        return null;
    }
//    @Override
//    public Object resolveArgument(Forest.Routing routing, Class<?> argumentType, RoutingContext routingContext, RequestBody requestBody) {
//        return null;
//    }
//    private ImmutableMap<String, RequestBodyParser<?>> contentTypeToRequestBodyParserMap = ImmutableMap.of();
//
//    @Override
//    public Object readRequestBody(RoutingContext context, Class<?> argumentClass) {
//        String contentType = context.parsedHeaders().contentType().component();
//        RequestBodyParser<?> parser = contentTypeToRequestBodyParserMap.get((contentType));
//        if (parser == null) {
//            throw HttpException.notAcceptable();
//        }
//        return parser.readRequestBody(context, argumentClass);
//    }
}
