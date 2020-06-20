package io.forestframework.config;

import com.google.common.collect.ImmutableMap;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.commons.math3.util.Pair;

import java.util.Map;
import java.util.Objects;

interface Converter<IN, OUT> {
    OUT convert(IN in, Class<? extends IN> inType, Class<? extends OUT> outType);
}

enum DefaultConverter implements Converter<Object, Object> {
    INSTANCE;

    private final Map<Pair<Class, Class>, Converter> converters = ImmutableMap.<Pair<Class, Class>, Converter>builder()
            .put(Pair.create(Object.class, String.class), new ObjectToString())
            .put(Pair.create(Object.class, JsonObject.class), new ObjectToJsonObject())
            .put(Pair.create(String.class, Enum.class), new StringToEnum())
            .put(Pair.create(Object.class, long.class), new ObjectToLong())
            .put(Pair.create(String.class, Long.class), new ObjectToLong())
            .put(Pair.create(Object.class, Integer.class), new ObjectToInteger())
            .put(Pair.create(Object.class, int.class), new ObjectToInteger())
            .put(Pair.create(Object.class, Boolean.class), new ObjectToBoolean())
            .put(Pair.create(Object.class, boolean.class), new ObjectToBoolean())
            .build();

    @Override
    public Object convert(Object obj, Class<?> inType, Class<?> outType) {
        if (outType.isAssignableFrom(inType)) {
            return obj;
        }
        for (Map.Entry<Pair<Class, Class>, Converter> entry : converters.entrySet()) {
            if (entry.getKey().getFirst().isAssignableFrom(inType) && entry.getKey().getSecond().isAssignableFrom(outType)) {
                return entry.getValue().convert(obj, inType, outType);
            }
        }
        throw new RuntimeException("Can't find matching converter: in: " + inType + ", out: " + outType);
    }
}

class ObjectToJsonObject implements Converter<Object, JsonObject> {
    @Override
    public JsonObject convert(Object obj, Class<?> inType, Class<? extends JsonObject> outType) {
        return new JsonObject(Json.CODEC.toString(obj));
    }
}

class ObjectToBoolean implements Converter<Object, Boolean> {
    @Override
    public Boolean convert(Object obj, Class<?> inType, Class<? extends Boolean> outType) {
        String s = obj.toString();
        return Boolean.parseBoolean(s);
    }
}

class ObjectToString implements Converter<Object, String> {
    @Override
    public String convert(Object o, Class<? extends Object> inType, Class<? extends String> outType) {
        return Objects.toString(o);
    }
}

class StringToEnum implements Converter<String, Enum> {
    @Override
    public Enum convert(String s, Class<? extends String> inType, Class<? extends Enum> outType) {
        try {
            return (Enum) outType.getMethod("valueOf", String.class).invoke(null, s);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}

class ObjectToInteger implements Converter<Object, Integer> {
    @Override
    public Integer convert(Object obj, Class<? extends Object> inType, Class<? extends Integer> outType) {
        String s = obj.toString();
        if (s.startsWith("0x")) {
            return Integer.valueOf(s.substring(2), 16);
        } else {
            return Integer.valueOf(s);
        }
    }
}

class ObjectToLong implements Converter<Object, Long> {
    @Override
    public Long convert(Object obj, Class<? extends Object> inType, Class<? extends Long> outType) {
        String s = obj.toString();
        if (s.startsWith("0x")) {
            return Long.valueOf(s.substring(2), 16);
        } else {
            return Long.valueOf(s);
        }
    }
}

