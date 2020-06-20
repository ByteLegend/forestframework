package io.forestframework;

import io.forestframework.http.Routing;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class ThymeleafRenderingProcessor implements RoutingResultProcessor {
    private final ThymeleafTemplateEngine engine;

    @Inject
    public ThymeleafRenderingProcessor(Vertx vertx) {
        this.engine = ThymeleafTemplateEngine.create(vertx);
        this.engine.getThymeleafTemplateEngine().setTemplateResolver(new ClassLoaderTemplateResolver() {
            {
                setPrefix("templates/");
                setSuffix(".html");
            }
        });
        this.engine.getThymeleafTemplateEngine().setLinkBuilder(new StandardLinkBuilder() {
            @Override
            protected String computeContextPath(
                    final IExpressionContext context, final String base, final Map<String, Object> parameters) {
                return "/";
            }
        });
    }

    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
        if (!(returnValue instanceof String)) {
            throw new RuntimeException("Return value processed by ThymeleafRenderingProcessor must be string!");
        }
        return engine.render(routingContext.data(), (String) returnValue)
                .compose(buffer -> {
                    routingContext.response().setChunked(true);
                    routingContext.response().write(buffer);
                    return Future.succeededFuture();
                });
    }
}
