package io.forestframework.benchmark

import io.forestframework.annotation.Get
import io.forestframework.annotation.GetPlainText
import io.forestframework.annotation.Intercept
import io.forestframework.benchmark.model.Fortune
import io.forestframework.benchmark.model.Message
import io.forestframework.benchmark.model.World
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.ext.pgclient.PgClientExtension
import io.forestframework.http.JsonResponseBody
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.buffer.impl.BufferImpl
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.sqlclient.executeAwait
import io.vertx.kotlin.sqlclient.executeBatchAwait
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min


fun main() {
    Forest.run(App::class.java)
}

val RESPONSE_TYPE_PLAIN = HttpHeaders.createOptimized("text/plain")
val RESPONSE_TYPE_HTML = HttpHeaders.createOptimized("text/html; charset=UTF-8")
val RESPONSE_TYPE_JSON = HttpHeaders.createOptimized("application/json")
val HEADER_SERVER = HttpHeaders.createOptimized("server")
val HEADER_DATE = HttpHeaders.createOptimized("date")
val HEADER_CONTENT_TYPE = HttpHeaders.createOptimized("content-type")
val HEADER_CONTENT_LENGTH = HttpHeaders.createOptimized("content-length")
val HELLO_WORLD = "Hello, world!"

val HELLO_WORLD_LENGTH = HttpHeaders.createOptimized("" + HELLO_WORLD.length)
val SERVER = HttpHeaders.createOptimized("forest")
val HELLO_WORLD_BUFFER: Buffer = BufferImpl.directBuffer(HELLO_WORLD, "UTF-8")

@ForestApplication(extensions = [PgClientExtension::class])
@Singleton
class App @Inject constructor(private val client: PgPool, vertx: Vertx) {
    var dateString = createDateHeader()
    val plaintextHeaders = arrayOf<CharSequence>(
        HEADER_CONTENT_TYPE, RESPONSE_TYPE_PLAIN,
        HEADER_SERVER, SERVER,
        HEADER_DATE, dateString,
        HEADER_CONTENT_LENGTH, HELLO_WORLD_LENGTH)

    init {
        vertx.setPeriodic(1000, Handler<Long> { id: Long? -> plaintextHeaders[5] = createDateHeader().also { dateString = it } })

//        runBlocking {
//            val sqls = javaClass.classLoader.getResourceAsStream("create-postgres.sql").bufferedReader().readText()
//            for (sql in sqls.split(";")) {
//                if (sql.isNotBlank()) {
//                    client.query(sql).executeAwait()
//                }
//            }
//        }
    }

    private val UPDATE_WORLD = "UPDATE world SET randomnumber=$1 WHERE id=$2"
    private val SELECT_WORLD = "SELECT id, randomnumber from WORLD where id=$1"
    private val SELECT_FORTUNE = "SELECT id, message from FORTUNE"

    private fun createDateHeader() = HttpHeaders.createOptimized(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()))

    @Intercept("/*")
    fun addServerAndDateHeader(response: HttpServerResponse) {
        response.headers().add(HEADER_SERVER, SERVER).add(HEADER_DATE, dateString)
    }

    @Get("/json")
    @JsonResponseBody
    fun json() = Message("Hello, World!").toBuffer()

    @GetPlainText("/plaintext")
    fun plaintext() = HELLO_WORLD_BUFFER

    @Get("/db")
    @JsonResponseBody
    suspend fun singleDatabaseQuery(response: HttpServerResponse): World {
        val rows: RowSet<Row> = client.preparedQuery(SELECT_WORLD).executeAwait(Tuple.of(randomWorld()))
        val iterator = rows.iterator()
        if (!iterator.hasNext()) {
            response.setStatusCode(404).end()
        }
        val row: Tuple = iterator.next()
        return World(row.getInteger(0), row.getInteger(1))
    }

    @Get("/queries")
    @JsonResponseBody
    suspend fun multipleDatabaseQuery(routingContext: RoutingContext): List<World> {
        val queries = parseParam(routingContext)
        return (1..queries).map {
            val result = client.preparedQuery(SELECT_WORLD).executeAwait(Tuple.of(randomWorld()))
            val row: Tuple = result.iterator().next()
            World(row.getInteger(0), row.getInteger(1))
        }
    }

    private fun parseParam(routingContext: RoutingContext): Int {
        return try {
            min(500, max(1, routingContext.request().getParam("queries").toInt()))
        } catch (e: Exception) {
            1
        }
    }

    @Get("/updates")
    @JsonResponseBody
    suspend fun updateDatabase(routingContext: RoutingContext): List<World> {
        val queries = parseParam(routingContext)
        val worlds = (1..queries).map {
            val id = randomWorld()
            val result = client.preparedQuery(SELECT_WORLD).executeAwait(Tuple.of(id))
            World(result.iterator().next().getInteger(0), randomWorld())
        }.sorted()

        val worldsToUpdate = worlds.map { Tuple.of(it.randomNumber, it.id) }
        client.preparedQuery(UPDATE_WORLD).executeBatchAwait(worldsToUpdate)
        return worlds
    }

    @Get("/fortunes")
    @RockerTemplateRendering
    suspend fun getFortunes(response: HttpServerResponse): List<Fortune> {
        val rows = client.preparedQuery(SELECT_FORTUNE).executeAwait().iterator()
        if (!rows.hasNext()) {
            response.setStatusCode(404).end("No results")
        }

        val fortunes = mutableListOf<Fortune>()
        while (rows.hasNext()) {
            val row = rows.next()
            fortunes.add(Fortune(row.getInteger(0), row.getString(1)))
        }
        fortunes.add(Fortune(0, "Additional fortune added at request time."));
        return fortunes.sorted()
    }

    private
    fun randomWorld(): Int {
        return 1 + ThreadLocalRandom.current().nextInt(10000)
    }

}

