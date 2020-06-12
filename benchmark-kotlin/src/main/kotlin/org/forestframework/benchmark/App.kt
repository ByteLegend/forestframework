package org.forestframework.benchmark

import io.reactiverse.kotlin.pgclient.getConnectionAwait
import io.reactiverse.kotlin.pgclient.preparedBatchAwait
import io.reactiverse.kotlin.pgclient.preparedQueryAwait
import io.reactiverse.pgclient.PgClient
import io.reactiverse.pgclient.PgConnection
import io.reactiverse.pgclient.PgPool
import io.reactiverse.pgclient.PgRowSet
import io.reactiverse.pgclient.Tuple
import io.vertx.core.buffer.Buffer
import io.vertx.core.buffer.impl.BufferImpl
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.forestframework.Forest
import org.forestframework.annotation.ForestApplication
import org.forestframework.annotation.Get
import org.forestframework.annotation.GetPlainText
import org.forestframework.annotation.Intercept
import org.forestframework.annotation.JsonResponseBody
import org.forestframework.annotation.QueryParam
import org.forestframework.annotation.TemplateRendering
import org.forestframework.ext.pgclient.PgClientExtension
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min


fun main() {
    Forest.run(App::class.java);
}

@ForestApplication(include = [PgClientExtension::class])
@Singleton
class App @Inject constructor(private val client: PgClient,
                              private val pool: PgPool) {
    private val RESPONSE_TYPE_PLAIN = HttpHeaders.createOptimized("text/plain")
    private val RESPONSE_TYPE_HTML = HttpHeaders.createOptimized("text/html; charset=UTF-8")
    private val RESPONSE_TYPE_JSON = HttpHeaders.createOptimized("application/json")
    private val HEADER_SERVER = HttpHeaders.createOptimized("server")
    private val HEADER_DATE = HttpHeaders.createOptimized("date")
    private val HEADER_CONTENT_TYPE = HttpHeaders.createOptimized("content-type")
    private val HEADER_CONTENT_LENGTH = HttpHeaders.createOptimized("content-length")
    private val HELLO_WORLD = "Hello, world!"

    private val HELLO_WORLD_LENGTH = HttpHeaders.createOptimized("" + HELLO_WORLD.length)
    private val SERVER = HttpHeaders.createOptimized("forest")
    private val HELLO_WORLD_BUFFER: Buffer = BufferImpl.directBuffer(HELLO_WORLD, "UTF-8")
    private val dateString = HttpHeaders.createOptimized(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()))
    private val plaintextHeaders = arrayOf<CharSequence>(
        HEADER_CONTENT_TYPE, RESPONSE_TYPE_PLAIN,
        HEADER_SERVER, SERVER,
        HEADER_DATE, dateString,
        HEADER_CONTENT_LENGTH, HELLO_WORLD_LENGTH)

    private val UPDATE_WORLD = "UPDATE world SET randomnumber=$1 WHERE id=$2"
    private val SELECT_WORLD = "SELECT id, randomnumber from WORLD where id=$1"
    private val SELECT_FORTUNE = "SELECT id, message from FORTUNE"

    @Intercept("/*")
    fun addServerAndDateHeader(response: HttpServerResponse) {
        response.headers().add(HEADER_SERVER, SERVER).add(HEADER_DATE, dateString)
    }

    @GetPlainText("/plaintext")
    fun plaintext() = HELLO_WORLD_BUFFER

    @Get("/db")
    @JsonResponseBody
    suspend fun singleDatabaseQuery(response: HttpServerResponse): World {
        val rows: PgRowSet = client.preparedQueryAwait(SELECT_WORLD, Tuple.of(randomWorld()))
        val iterator = rows.iterator()
        if (!iterator.hasNext()) {
            response.setStatusCode(404).end()
        }
        val row: Tuple = iterator.next()
        return World(row.getInteger(0), row.getInteger(1))
    }

    @Get("/queries")
    @JsonResponseBody
    suspend fun multipleDatabaseQuery(@QueryParam("queries", defaultValue = "1") param: Int): List<World> {
        val queries = min(500, max(1, param))
        return (1..queries).map {
            val result = client.preparedQueryAwait(SELECT_WORLD, Tuple.of(randomWorld()))
            val row: Tuple = result.iterator().next()
            World(row.getInteger(0), row.getInteger(1))
        }
    }

    @Get("/updates")
    @JsonResponseBody
    suspend fun updateDatabase(@QueryParam("queries", defaultValue = "1") param: Int): List<World> {
        val queries = min(500, max(1, param))
        val connection: PgConnection = pool.getConnectionAwait()
        val worlds = (1..queries).map {
            val id = randomWorld()
            val result = connection.preparedQueryAwait(SELECT_WORLD, Tuple.of(id))
            World(result.iterator().next().getInteger(0), randomWorld())
        }.sorted()

        val worldsToUpdate = worlds.map { Tuple.of(it.randomNumber, it.id) }
        connection.preparedBatchAwait(UPDATE_WORLD, worldsToUpdate)
        return worlds
    }

    @Get("/fortunes")
//    @TemplateRendering
    suspend fun getFortunes(response: HttpServerResponse, routingContext: RoutingContext): String {
        val rows = client.preparedQueryAwait(SELECT_FORTUNE).iterator()
        if (!rows.hasNext()) {
            response.setStatusCode(404).end("No results");
        }

        val fortunes = mutableListOf<Fortune>()
        while (rows.hasNext()) {
            val row = rows.next()
            fortunes.add(Fortune(row.getInteger(0), row.getString(1)))
        }
        fortunes.add(Fortune(0, "Additional fortune added at request time."));
        routingContext.put("fortunes", fortunes.sorted())
        return "fortunes";
    }

    private
    fun randomWorld(): Int {
        return 1 + ThreadLocalRandom.current().nextInt(10000)
    }

}

/**
 * Constructs a new world object with the given parameters.
 *
 * @param id the ID of the world
 * @param randomNumber the random number of the world
 */
data class World(val id: Int, val randomNumber: Int) : Comparable<World> {
    override fun compareTo(other: World): Int {
        return id.compareTo(other.id)
    }
}

data class Fortune(val id: Int, val message: String) : Comparable<Fortune> {
    override fun compareTo(other: Fortune): Int {
        return message.compareTo(other.message)
    }
}