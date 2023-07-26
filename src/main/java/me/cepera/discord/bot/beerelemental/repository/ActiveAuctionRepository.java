package me.cepera.discord.bot.beerelemental.repository;

import me.cepera.discord.bot.beerelemental.model.ActiveAuction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ActiveAuctionRepository {

    Mono<Void> saveAuction(ActiveAuction auction);

    Flux<ActiveAuction> getEndedActiveAuctions();

    Mono<Void> deleteAuction(ActiveAuction auction);

}
