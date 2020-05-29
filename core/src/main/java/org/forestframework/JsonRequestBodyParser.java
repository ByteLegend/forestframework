package org.forestframework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.RoutingContext;

public class JsonRequestBodyParser implements RequestBodyParser<Object> {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object readRequestBody(RoutingContext context, Class<?> argumentClass) {
        try {
            return argumentClass.cast(objectMapper.readValue(context.getBodyAsString(), argumentClass));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
