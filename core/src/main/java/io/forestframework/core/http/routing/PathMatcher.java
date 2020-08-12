package io.forestframework.core.http.routing;

import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For internal use only.
 *
 * Supports two kinds of path params:
 * <ul>
 *     <li>"/user/:userId/order/:orderId", where you can extract "userId" and "orderId"</li>
 *     <li>"/users/*", you can extract the parameter "1" from "/users/1", and "user/1" from "/users/user/1"</li>
 * </ul>
 */
@API(status = API.Status.INTERNAL, since = "0.1")
public interface PathMatcher {
    // We assume the cache populating only happens at startup phase
    Map<String, PathMatcher> CACHE = new HashMap<>();

    boolean matches(String path);

    String pathParam(String path, String name);

    static PathMatcher fromPattern(String pattern) {
        if (pattern == null) {
            throw new NullPointerException();
        }

        PathMatcher cached = CACHE.get(pattern);
        if (cached == null) {
            String[] segments = pattern.split("/");

            StringBuilder sb = new StringBuilder();

            int starCount = StringUtils.countMatches(pattern, '*');
            for (String segment : segments) {
                if (!segment.isEmpty()) {
                    sb.append("/");
                    if (segment.startsWith(":")) {
                        sb.append("(?<").append(segment.substring(1)).append(">[^/]*)");
                    } else {
                        sb.append(segment.replace("*", starCount == 1 ? "(?<wildcard>.*)" : "(.*)"));
                    }
                }
            }

            String regex = sb.toString();
            if (regex.equals(pattern)) {
                cached = new TextPathMatcher(pattern);
            } else {
                cached = new RegexBasedPathMatcher(pattern, regex, starCount);
            }

            CACHE.put(pattern, cached);
        }
        return cached;
    }

    class TextPathMatcher implements PathMatcher {
        private final String path;

        private TextPathMatcher(String path) {
            this.path = path;
        }

        @Override
        public boolean matches(String path) {
            return this.path.equals(path);
        }

        @Override
        public String pathParam(String path, String name) {
            return null;
        }
    }

    class RegexBasedPathMatcher implements PathMatcher {
        private final String pathPattern;
        private final Pattern pattern;
        private final int starCount;

        private RegexBasedPathMatcher(String pathPatten, String regex, int starCount) {
            this.pathPattern = pathPatten;
            this.pattern = Pattern.compile(regex);
            this.starCount = starCount;
        }

        @Override
        public boolean matches(String path) {
            return pattern.matcher(path).matches();
        }

        @Override
        public String pathParam(String path, String name) {
            Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                if ("*".equals(name)) {
                    if (starCount > 1) {
                        throw new IllegalArgumentException("You have more than one * in path " + pathPattern);
                    }
                    return matcher.group("wildcard");
                } else {
                    return matcher.group(name);
                }
            } else {
                return null;
            }
        }
    }
}
