# Forest Framework - a high-concurrency JVM web framework 

## TLDR;

`Forest Framework` = `Vert.x` + `Guice`, with special support for Kotlin coroutines - "Code like sync, works like async", just like Loom.

Heads up: if you're not going to build an application for millions of concurrent users, go to [Spring Boot](https://spring.io/projects/spring-boot).

## Forest Framework is designed for high-concurrency 

Without [Project Loom](https://openjdk.java.net/projects/loom/), we have only two options for JVM-based application:

- Synchronized application with heavy threads: easy to write, bad for performance.
- Async application with callback hell: much better performance, disastrous readability and maintainability.

Forest Framework support Kotlin coroutines first workflow: you write suspend synchronized-like methods, then everything works in async way.

This is a Forest application example acting as a reverse proxy which redirects `github.com` to `localhost:8080`. 
As you can see, we are writing sync code that works in non-blocking way - the code runs in `Vert.x` event loop thread and doesn't block at all.
And we avoid the usual callback hell in `Vert.x`!

```
@ForestApplication
class App @Inject constructor(vertx: Vertx) {

    private val webClient = WebClient.create(vertx)
    
    @GetHtml("/")
    suspend fun index(): String {
        return webClient.getAbs("https://github.com").send().await().bodyAsString()
    }
}
```

