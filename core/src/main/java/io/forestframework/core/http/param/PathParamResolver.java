package io.forestframework.core.http.param;

import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import io.forestframework.annotationmagic.AnnotationMagic;
import io.forestframework.core.http.routing.Routing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolve parameters from request url path. Three kins of path parameters are supported:
 * 1. /user/:id -> @PathParam("id")
 * 2. Regex path: \/js\/(?<file>.+) -> @PathParam("file")
 * 3. Wildcard path: /static/* -> @PathParam("*")
 */
public class PathParamResolver implements RoutingParameterResolver<Object> {
    private Map<String, Pattern> wildcardPatternCache = new ConcurrentHashMap<>();

    @Override
    public Object resolveArgument(Routing routing, RoutingContext routingContext, int paramIndex) {
        PathParam pathParam = AnnotationMagic.getOneAnnotationOnMethodParameter(routing.getHandlerMethod(), paramIndex, PathParam.class);

        if ("*".equals(pathParam.value())) {
            return resolveWildcardFromUrl(routing, routingContext);
        } else {
            return routingContext.request().getParam(pathParam.value());
        }
    }

    private String resolveWildcardFromUrl(Routing routing, RoutingContext routingContext) {
        if (StringUtils.isNotBlank(routing.getRegexPath())) {
            throw new IllegalArgumentException("* parameter is not supported in regexPath!");
        }
        if (StringUtils.countMatches(routing.getPath(), '*') > 1) {
            throw new IllegalArgumentException("Found more than 1 wildcard in " + routing.getPath() + ", can't decide which one.");
        }
        if (!routing.getPath().contains("*")) {
            return null;
        }

        Matcher matcher = getPattern(routing.getPath()).matcher(routingContext.request().absoluteURI());
        if (matcher.find()) {
            return matcher.group("wildcard");
        } else {
            return null;
        }
    }

    private Pattern getPattern(String path) {
        Pattern ret = wildcardPatternCache.get(path);
        if (ret == null) {
            int wildcardIndex = path.indexOf('*');

            String regex = "\\Q" + path.substring(0, wildcardIndex) + "\\E" + "(?<wildcard>.*)" + "\\Q" + path.substring(wildcardIndex + 1) + "\\E";
            ret = Pattern.compile(regex);
            wildcardPatternCache.put(path, ret);
        }
        return ret;
    }
}
