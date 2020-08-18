package io.forestframework.core.http.param;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import io.forestframework.core.http.OptimizedHeaders;
import io.forestframework.core.http.PlainHttpContext;
import io.forestframework.core.http.WebContext;
import io.forestframework.core.http.routing.Routing;
import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.buffer.Buffer;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Singleton
public class JsonRequestBodyParser implements ContentTypeAwareRoutingParameterResolver<Object> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String contentType() {
        return "application/json";
    }

    @Override
    @SuppressWarnings("ALL")
    public Object resolveParameter(WebContext webContext, Routing routing, int paramIndex) {
        PlainHttpContext context = (PlainHttpContext) webContext;

        String contentType = context.request().getHeader(OptimizedHeaders.HEADER_CONTENT_TYPE);
        if (contentType != null) {
            MediaType mediaType = MediaType.parse(contentType);
            if (mediaType.charset().isPresent() && mediaType.charset().get() != StandardCharsets.UTF_8) {
                throw new IllegalArgumentException("Sorry, we only support UTF-8.");
            }
        }

        Class<?> paramType = routing.getHandlerMethod().getParameterTypes()[paramIndex];
        if (paramType == String.class) {
            return context.request().body().map(Buffer::toString);
        } else if (paramType == Buffer.class) {
            return context.request().body();
        }
        return context.request().body().map((Buffer buffer) -> {
            try {
                return objectMapper.readValue((InputStream) new ByteBufInputStream(buffer.getByteBuf()), paramType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
