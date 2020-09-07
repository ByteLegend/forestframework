# Documentation

## 路由处理器

在路由（Route）中，我们通过路由处理器来接收和处理匹配成功的请求。路由处理器分为：

* 主处理器
* 前处理器
* 错误处理器
* 后处理器

不同类型的处理器以适当的顺序形成处理器链路（handlers chain），共同完成对请求的接收和处理。

## 主处理器（Handler）

路由必须有一个与之关联的主处理器（Handler）来接收并处理请求。
因此，程序中至少有一个主处理器（Handler），否则程序运行会报错：`404 NOT_FOUND`。
可以通过 `@Route(type = RoutingType.HANDLER)` 注解来设置主处理器。
主处理器通过调用对应的方法来处理请求。方法必须是 `public` 的，否则程序运行也会报错：`404 NOT_FOUND`。

```
@ForestApplication
public class Example {

    @Route(path = "/foo", type = RoutingType.HANDLER)
    public void handle(HttpServerResponse response) {
        // 路径匹配成功的请求会通过调用主处理器的 handler 方法处理请求
        // 返回字符串作为响应
        response.write("handler is handling", "UTF-8");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
``` 

`@Route` 注解默认处理所有的 `HTTP` 方法请求。
对于 `HTTP Get` 请求，还可以用 `@Get` 来设置主处理器。
同理，`@Post` 标识的主处理器可以处理 `HTTP Post` 方法的请求。

```
@ForestApplication
public class Example {

    @Get("/foo")
    public void handle(HttpServerResponse response) {
        response.write("handler is handling", "UTF-8");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
``` 

它等价于：

```
@ForestApplication
public class Example {

    // equivalent to @Get("/foo")
    @Route(path = "/foo", type = RoutingType.HANDLER, methods = HttpMethod.GET)
    public void handle(HttpServerResponse response) {
        response.write("handler is handling", "UTF-8");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

程序中可以有多个主处理器，处理同一个路径的请求。
这时需要通过 `@Route` 注解的 `order` 属性来定义多个主处理器之间的先后顺序。
`order` 属性值可以是任意整数，包括负数。数值越小，优先级越高，优先执行。

```
@ForestApplication
public class Example {

    // handler2 的 order 属性值大于 handler1，因此其后执行
    @Route(path = "/foo", type = RoutingType.HANDLER, methods = HttpMethod.GET, order = 1)
    public void handle2(HttpServerResponse response) {
        response.write("handler2 is handling next\n", "UTF-8");
    }

    // handler1 的 order 属性值小于 handler2，因此优先执行
    @Route(path = "/foo", type = RoutingType.HANDLER, methods = HttpMethod.GET, order = 0)
    public void handle1(HttpServerResponse response) {
        response.write("handler1 is handling first\n", "UTF-8");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

## 前处理器（PreHandler）

一个路由可以没有与之对应对前处理器（PreHandler）。前处理器不是必须的。
可以根据需要，在主处理器接收请求之前对请求进行拦截，对请求进行相应的处理，比如鉴权等操作。
可以使用 `@PreHandler` 注解设置前处理器，默认拦截所有路径匹配的 `HTTP` 方法的请求。

```
@ForestApplication
public class Example {

    @PreHandler("/foo")
    public void prehandle(HttpServerResponse response) {
        // 前处理器
        response.write("prehandler is handling");
    }

    @Route(path = "/foo", type = RoutingType.HANDLER)
    public void handle2(HttpServerResponse response) {
        // 主处理器
        response.write("handler is handling", "UTF-8");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

还可以通过 `@Route(type = RoutingType.PRE_HANDLER)` 来设置前处理器。

```
@ForestApplication
public class Example {

    @Route(path = "/foo", type = RoutingType.PRE_HANDLER)
    public void prehandle(HttpServerResponse response) {
        response.write("prehandler is handling");
    }

    @Route(path = "/foo", type = RoutingType.HANDLER)
    public void handle2(HttpServerResponse response) {
        response.write("handler is handling");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

上面的例子中，前处理器实现了对请求的拦截，和简单的处理，并把请求传递给其他的前处理器或主处理器。
如果前处理器的返回值类型为布尔值，当对请求的处理结果为 `false` 的时候，
请求不会传递给其他的前处理器或主处理器，实现对请求的彻底拦截（short-circuit）。

```
@ForestApplication
public class Example {

    @Route(path = "/foo", type = RoutingType.PRE_HANDLER, order = 1)
    public boolean prehandle1(HttpServerResponse response) {
        // 返回值为 false，不会执行其他的前处理器和主处理器
        response.write("prehandler1 is handling and short-circuits the routing");
        return false;
    }

    @Route(path = "/foo", type = RoutingType.PRE_HANDLER, order = 2)
    public void prehandle2(HttpServerResponse response) {
        // 不会执行
        response.write("should not be here");
    }

    @Route(path = "/foo", type = RoutingType.HANDLER)
    public void handle2(HttpServerResponse response) {
        // 不会执行
        response.write("should not be here");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

合法的前处理器的返回值类型是 `void` / `boolean` / `Boolean` 
/ `Future<Void>` / `CompletableFuture<Void>` / `Future<Boolean>` / `CompletableFuture<Boolean>` 
其中的一种。其他返回值类型都是不合法的。

```
@ForestApplication
public class Example {

    @Route(path = "/foo", type = RoutingType.PRE_HANDLER)
    public String prehandle(HttpServerResponse response) {
        // 不合法的 PreHandler 返回值类型
        response.write("prehandler is handling");
        return "illegal prehandler return type";
    }

    @Route(path = "/foo", type = RoutingType.HANDLER)
    public void handle2(HttpServerResponse response) {
        response.write("handler is handling", "UTF-8");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

```
@ForestApplication
public class Example {

    @Route(path = "/foo", type = RoutingType.PRE_HANDLER)
    public CompletableFuture<Integer> prehandle(HttpServerResponse response) {
        // 不合法的 PreHandler 返回值类型
        response.write("prehandler is handling");
        return new CompletableFuture<>();
    }

    @Route(path = "/foo", type = RoutingType.HANDLER)
    public void handle2(HttpServerResponse response) {
        response.write("handler is handling", "UTF-8");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

当需要使用多个前处理器的时候，通过前处理器的 `order` 属性定义前处理器的执行顺序。

```
@ForestApplication
public class Example {

    @Route(path = "/foo", type = RoutingType.PRE_HANDLER, order = 2)
    public void prehandle2(HttpServerResponse response) {
        response.write("prehandler2 is handling next\n");
    }

    @Route(path = "/foo", type = RoutingType.PRE_HANDLER, order = 1)
    public void prehandle1(HttpServerResponse response) {
        response.write("prehandler1 is handling first\n");
    }

    @Route(path = "/foo", type = RoutingType.HANDLER)
    public void handle2(HttpServerResponse response) {
        response.write("handler is handling");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

## 错误处理器 （Error Handler）

当处理器抛出异常或者程序执行过程中发生异常，会触发错误处理器。
比如请求的路径不匹配，请求的方法不匹配，请求被处理器处理的过程中抛出异常，
导致程序无法正常执行等。
如果没有自定义错误处理器，会触发系统默认的错误处理器。
通过 `@OnError` 注解可以自定义错误处理器，自定义处理器默认处理状态码为 500 的错误。
自定义的错误处理器可以设置在某个请求路径上，也可以设置为匹配所有路径。


```
@ForestApplication
public class Example {

    @PreHandler("/foo")
    public void prehandle(HttpServerResponse response) {
        throw new RuntimeException("prehandler throws an error");
    }

    @Route(path = "/foo", type = RoutingType.HANDLER)
    public void handle(HttpServerResponse response) {
        response.write("handler is handling");
    }

    @OnError("/**")
    public void onError(HttpServerResponse response) {
        response.write("error handler is handling");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

如果自定义的错误处理器需要处理除状态码 500 以外的错误，那么需要指定错误处理器的 `statusCode` 属性。

```
@ForestApplication
public class Example {

    // 当客户端发送 Get 请求的时候，会触发自定义 405 错误处理器

    @PreHandler(value = "/foo", methods = HttpMethod.POST)
    public void prehandle(HttpServerResponse response) {
        response.write("prehandler is handling");
    }

    @Route(path = "/foo", type = RoutingType.HANDLER, methods = HttpMethod.POST)
    public void handle(HttpServerResponse response) {
        response.write("handler is handling");
    }

    @OnError(path = "/foo", statusCode = HttpStatusCode.METHOD_NOT_ALLOWED)
    public void handleError(HttpServerResponse response) {
        // 处理状态码为 405 的错误
        response.write("error handler foo is handling");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

## 后处理器（PostHandler）

后处理器在前处理器和主处理器之后执行。可以用于资源回收等清理工作。

```
@ForestApplication
public class Example {

    @PreHandler(value = "/foo")
    public void prehandle(HttpServerResponse response) {
        response.write("prehandler is handling");
    }

    @Route(path = "/foo", type = RoutingType.HANDLER)
    public void handle(HttpServerResponse response) {
        response.write("handler is handling");
    }

    @PostHandler("/foo")
    public void postHandle(HttpServerResponse response) {
        response.write("posthandler is handling");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
}
```

如果发生异常，后处理器在错误处理器之后执行。

```
@ForestApplication
public class Example {

    @PreHandler("/foo")
    public void prehandle(HttpServerResponse response) {
        response.write("prehandler is handling");
    }

    @Route(path = "/foo", type = RoutingType.HANDLER)
    public void handle(HttpServerResponse response) {
        throw new RuntimeException("handler throws an error");
    }

    @OnError("/foo")
    public void handleError(HttpServerResponse response) {
        response.write("error handler is handling");
    }

    @PostHandler("/foo")
    public void postHandle(HttpServerResponse response) {
        response.write("posthandler is handling");
    }

    public static void main(String[] args) {
        Forest.run(Example.class);
    }
```

## Route mapping  路由映射
### 基本路径匹配


路由映射取决于请求方法，请求路径，和请求体的多媒体类型。你可以用基本路径匹配路由，比如，`@Get("/some/path")` 
或者 `@Route(method=GET, path="/some/path", type=PRE_HANDLER)`.

```
@Get("/some/path")
public void handler(HttpServerRequest request) {
    // This handler will be called for the following request paths:
    
    // `/some/path`
    // `/some/path/`
    // 
    // but not:
    // `/some/path/subdir`
}
```

此外，我们还可以用 `*` or `**` 这样的 [Ant style pattern](https://ant.apache.org/manual/dirtasks.html)   
来匹配路径，比如：

```
@Get("/some/path/*")
public void handler(HttpServerRequest request) {
    // This handler will be called for any path that starts with
    // `/some/path/`, e.g.
    
    // `/some/path/subdir`
    // `/some/path/subdir/blah.html`
    //
    // but not:
    // `/some/path`
    // `/some`
    // `/some/bath`
}
```

```
@PreHandler("/**")
public void preHandler(HttpServerRequest request) {
    // This pre-handler will be called for any path that starts with
    // `/`, e.g.
        
        // `/path`
        // `/index.html`
        // `/1.js`
        // 
        // but not:
        // `/static/1.js`
}
```

### 基于正则表达式的路由匹配

正则表达式是一个强大的工具，可以匹配路径和参数捕获。

```
@Get("/.*foo")
public void handler(HttpServerRequest request) {
    // This handler will be called for any request path ending with 'foo':
    
    // `/some/path/foo`
    // `/foo`
    // `/foo/bar/wibble/foo`
    // `/bar/foo`

    // But not:
    // `/bar/wibble`
}
```

```
@Get("\\/([^\\/]+)\\/([^\\/]+)")
public void handler(HttpServerRequest request) {
    // This regular expression matches paths that start with something like:
    // "/foo/bar" 
}
```
