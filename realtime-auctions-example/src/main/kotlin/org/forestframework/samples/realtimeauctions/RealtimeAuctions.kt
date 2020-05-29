package org.forestframework.samples.realtimeauctions

import io.vertx.core.eventbus.EventBus
import io.vertx.core.shareddata.LocalMap
import io.vertx.core.shareddata.SharedData
import io.vertx.ext.web.RoutingContext
import org.forestframework.Forest
import org.forestframework.annotation.ForestApplication
import org.forestframework.annotation.Get
import org.forestframework.annotation.Intercept
import org.forestframework.annotation.JsonResponseBody
import org.forestframework.annotation.Patch
import org.forestframework.annotation.PathParam
import org.forestframework.annotation.RequestBody
import org.forestframework.http.HttpException
import org.forestframework.http.HttpStatusCode
import java.math.BigDecimal
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@ForestApplication
class RealtimeAuctions {
}

fun main() {
    Forest.run(RealtimeAuctions::class.java)
}

@Singleton
class AuctionHandler(private val repository: AuctionRepository, private val validator: AuctionValidator) {
    @Get("/auctions/:id")
    @JsonResponseBody(pretty = true)
    fun handleGetAuction(context: RoutingContext, @PathParam("id") auctionId: String): Auction {
        return repository.getById(auctionId).orElseThrow { HttpException(HttpStatusCode.NOT_FOUND) }
    }

    @Patch("/auctions/:id")
    fun handleChangeAuctionPrice(eventBus: EventBus,
                                 @PathParam("id") auctionId: String,
                                 @RequestBody body: Map<String, Any>,
                                 @RequestBody bodyString: String) {
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

    @Intercept("/auctions/:id")
    fun initAuctionInSharedData(@PathParam("id") auctionId: String) {
        val auction = repository.getById(auctionId)
        if (!auction.isPresent) {
            repository.save(Auction(auctionId))
        }
    }
}


@Singleton
class AuctionRepository @Inject constructor(private val sharedData: SharedData) {
    fun getById(auctionId: String): Optional<Auction> {
        val auctionSharedData = sharedData.getLocalMap<String, String>(auctionId)
        return Optional.of(auctionSharedData)
            .filter { m: LocalMap<String?, String?> -> !m.isEmpty() }
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

@Singleton
class AuctionValidator @Inject constructor(private val repository: AuctionRepository) {
    fun validate(auction: Auction): Boolean {
        val auctionDatabase = repository.getById(auction.id)
            .orElseThrow { AuctionNotFoundException(auction.id) }
        return auctionDatabase.price < auction.price
    }
}
