package org.forestframework.config;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 For internal use. Don't use Apache Commons BeanUtils, it has known bugs in our scenarios.
 */
public class PropertyUtils {
    public static Object getProperty(Object obj, String key) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return ((Map) obj).get(key);
        }
        try {
            return getPropertyGetter(obj, key).invoke(obj);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void setProperty(Object bean, String key, Object value) {
        if (bean == null) {
            throw new NullPointerException("Try to set null object property, key: " + key + ", value: " + value);
        }
        if (bean instanceof Map) {
            ((Map) bean).put(key, value);
            return;
        }
        try {
            if (value instanceof List) {
                setListProperty(bean, key, (List) value);
            } else {
                Method propertySetter = getPropertySetter(bean, key).orElseThrow(() -> new IllegalStateException("Can't find setter " + key + " from " + bean + "!"));
                Class<?> parameterType = propertySetter.getParameterTypes()[0];
                if (value == null || parameterType == value.getClass()) {
                    propertySetter.invoke(bean, value);
                } else {
                    propertySetter.invoke(bean, DefaultConverter.INSTANCE.convert(value, value.getClass(), parameterType));
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void setListProperty(Object bean, String key, List<?> list) throws Exception {
        Optional<Method> setter = getPropertySetter(bean, key);
        if (setter.isPresent()) {
            Type paramRawType = setter.get().getGenericParameterTypes()[0];
            if (paramRawType instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) paramRawType;
                assertTrue(pType.getRawType() == List.class || pType.getRawType() == Set.class, "Setter " + setter.get() + " raw type must be List or Set!");
                setter.get().invoke(bean, getListOrSet(list, pType.getRawType(), pType.getActualTypeArguments()[0]));
            } else {
                assertTrue(paramRawType == List.class || paramRawType == Set.class, "Setter " + setter.get() + " raw type must be List or Set!");
                setter.get().invoke(bean, getListOrSet(list, paramRawType, Object.class));
            }
        } else {
            // clear() then add
            Method adder = getPropertyAdder(bean, key).orElseThrow(() -> new IllegalStateException("Can't find setter for " + bean.getClass() + "." + key));
            Object property = getProperty(bean, key);
            assertTrue(property instanceof Collection, "Bean " + bean + "'s " + key + " property must be Collection!");
            ((Collection) property).clear();

            for (Object element : list) {
                adder.invoke(bean, DefaultConverter.INSTANCE.convert(element, Object.class, adder.getParameterTypes()[0]));
            }
        }
    }

    private static Object getListOrSet(List<?> list, Type listOrSetRawType, Type listOrSetGenericType) {
        if (listOrSetRawType == List.class) {
            return list.stream().map(e -> DefaultConverter.INSTANCE.convert(e, e.getClass(), (Class) listOrSetGenericType)).collect(Collectors.toList());
        } else {
            return list.stream().map(e -> DefaultConverter.INSTANCE.convert(e, e.getClass(), (Class) listOrSetGenericType)).collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new IllegalStateException(message);
        }
    }

    private static Optional<Method> getPropertyAdderOrSetter(Object bean, String methodName) {
        return Stream.of(bean.getClass().getMethods())
                .filter(method -> methodName.equals(method.getName()))
                .filter(method -> method.getParameters().length == 1)
                .findFirst();
    }

    private static String singularize(String plural) {
        if (plural.endsWith("s")) {
            return plural.substring(0, plural.length() - 1);
        } else {
            return plural;
        }
    }

    // addXXX
    private static Optional<Method> getPropertyAdder(Object bean, String key) {
        String methodName = "add" + StringUtils.capitalize(singularize(key));
        return getPropertyAdderOrSetter(bean, methodName);
    }

    private static Optional<Method> getPropertySetter(Object bean, String key) {
        return getPropertyAdderOrSetter(bean, "set" + StringUtils.capitalize(key));
    }

    public static Method getPropertyGetter(Object bean, String key) {
        return Stream.of(bean.getClass().getMethods())
                .filter(method -> isGetter(method, key))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Can't find setter " + key + " from " + bean + "!"));
    }

    private static boolean isGetter(Method method, String key) {
        if (method.getParameters().length != 0) {
            return false;
        } else if (method.getName().equals("is" + StringUtils.capitalize(key))) {
            return method.getReturnType() == Boolean.class || method.getReturnType() == boolean.class;
        } else {
            return method.getName().equals("get" + StringUtils.capitalize(key));
        }
    }
}
