package io.forestframework.example.realtimeauctions

import com.fasterxml.jackson.databind.ObjectMapper
import io.forestframework.core.Component
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.core.http.HttpException
import io.forestframework.core.http.HttpStatusCode
import io.forestframework.core.http.Router
import io.forestframework.core.http.bridge.Bridge
import io.forestframework.core.http.bridge.BridgeEvent
import io.forestframework.core.http.bridge.BridgeEventType
import io.forestframework.core.http.param.JsonRequestBody
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.result.GetJson
import io.forestframework.core.http.routing.Patch
import io.forestframework.core.http.routing.PreHandler
import io.forestframework.ext.core.WithStaticResource
import io.vertx.core.eventbus.EventBus
import io.vertx.core.shareddata.LocalMap
import io.vertx.core.shareddata.SharedData
import java.math.BigDecimal
import java.util.Optional
import javax.inject.Inject

@WithStaticResource
@ForestApplication
class RealtimeAuctionsApp

fun main() {
    Forest.run(RealtimeAuctionsApp::class.java)
}

@Router
class EventBusHandler @Inject constructor(private val repository: AuctionRepository) {
    val objectMapper = ObjectMapper()
    @Bridge("/eventbus")
    fun bridgeEvent(event: BridgeEvent) {
        if (event.type() == BridgeEventType.SOCKET_CREATED) {
            println("A socket was created")
        }

        event.complete(true)
    }
}

@Router("/api")
class AuctionHandler @Inject constructor(private val repository: AuctionRepository, private val validator: AuctionValidator) {

    @GetJson(value = "/auctions/:id", pretty = true, respond404IfNull = true)
    fun handleGetAuction(@PathParam("id") auctionId: String): Auction? {
        return repository.getById(auctionId).orElse(null)
    }

    @Patch("/auctions/:id")
    fun handleChangeAuctionPrice(
        eventBus: EventBus,
        @PathParam("id") auctionId: String,
        @JsonRequestBody body: Map<String, Any>,
        @JsonRequestBody bodyString: String
    ) {
        val auctionRequest = Auction(
            auctionId,
            BigDecimal(body["price"].toString())
        )
        if (validator.validate(auctionRequest)) {
            repository.save(auctionRequest)
            eventBus.publish("auction.$auctionId", bodyString)
        } else {
            throw HttpException(HttpStatusCode.UNPROCESSABLE_ENTITY)
        }
    }

    @PreHandler("/auctions/:id")
    fun initAuctionInSharedData(@PathParam("id") auctionId: String) {
        val auction = repository.getById(auctionId)
        if (!auction.isPresent) {
            repository.save(Auction(auctionId))
        }
    }
}

@Component
class AuctionRepository @Inject constructor(private val sharedData: SharedData) {
    fun getById(auctionId: String): Optional<Auction> {
        val auctionSharedData = sharedData.getLocalMap<String, String>(auctionId)
        return Optional.of(auctionSharedData)
            .filter { m: LocalMap<String, String> -> !m.isEmpty() }
            .map { auction: LocalMap<String, String> -> convertToAuction(auction) }
    }

    fun save(auction: Auction) {
        val auctionSharedData: LocalMap<String, String> = sharedData.getLocalMap(auction.id)
        auctionSharedData["id"] = auction.id
        auctionSharedData["price"] = auction.price.toString()
    }

    private fun convertToAuction(auction: LocalMap<String, String>) = Auction(
        auction.getValue("id"),
        BigDecimal(auction["price"])
    )
}

data class Auction(val id: String, val price: BigDecimal) {
    constructor(auctionId: String) : this(auctionId, BigDecimal.ZERO)
}

class AuctionNotFoundException(auctionId: String) : RuntimeException("Auction not found: $auctionId")

@Component
class AuctionValidator @Inject constructor(private val repository: AuctionRepository) {
    fun validate(auction: Auction): Boolean {
        val auctionDatabase = repository.getById(auction.id)
            .orElseThrow { AuctionNotFoundException(auction.id) }
        return auctionDatabase.price < auction.price
    }
}
