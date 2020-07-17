package io.forestframework.core.http.result;

import io.forestframework.core.http.FastRoutingCompatible;
import io.forestframework.core.http.routing.Routing;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Singleton;

@Singleton
@FastRoutingCompatible
public class ThymeleafRenderingProcessor implements RoutingResultProcessor {
    @Override
    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
        return null;
    }
//    private final ThymeleafTemplateEngine engine;
//
//    @Inject
//    public ThymeleafRenderingProcessor(Vertx vertx) {
//        this.engine = ThymeleafTemplateEngine.create(vertx);
//        this.engine.getThymeleafTemplateEngine().setTemplateResolver(new ClassLoaderTemplateResolver() {
//            {
//                setPrefix("templates/");
//                setSuffix(".html");
//            }
//        });
//        this.engine.getThymeleafTemplateEngine().setLinkBuilder(new StandardLinkBuilder() {
//            @Override
//            protected String computeContextPath(
//                    final IExpressionContext context, final String base, final Map<String, Object> parameters) {
//                return "/";
//            }
//        });
//    }
//
//    @Override
//    public Object processResponse(RoutingContext routingContext, Routing routing, Object returnValue) {
//        if (!(returnValue instanceof String)) {
//            throw new RuntimeException("Return value processed by ThymeleafRenderingProcessor must be string!");
//        }
//        return engine.render(routingContext.data(), (String) returnValue)
//                .compose(buffer -> {
//                    routingContext.response().end(buffer);
//                    return Future.succeededFuture();
//                });
//    }
}
