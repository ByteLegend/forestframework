package io.forestframework.ext.core;

import com.google.inject.Injector;
import io.forestframework.ext.api.Extension;

/**
 * Adds support for catching particular {@link io.forestframework.core.http.HttpException}s ans returning corresponding HTTP responses.
 *
 * For example, you want to return 401 upon unauthorized requests, you just need to throw an exception
 * in handler method:
 *
 * <pre>
 * {@literal @}GetJson("/admin")
 * public Result admin() {
 *     if (unauthorized()) {
 *         throw HttpException.unauthorized();
 *     }
 *     // ... other code
 * }
 * </pre>
 */
public class HttpExceptionExtension implements Extension {
    @Override
    public void afterInjector(Injector injector) {

    }
}
