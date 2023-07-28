package me.cepera.discord.bot.beerelemental.repository;

import java.util.List;

import me.cepera.discord.bot.beerelemental.model.Kingdom;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KingdomRepository {

    Flux<Kingdom> getKingdoms(long guildId);

    Mono<Kingdom> getKingdom(int id);

    Mono<Kingdom> getKingdomByName(long guildId, String name);

    Mono<Kingdom> getKingdomByRole(long guildId, long roleId);

    Flux<Kingdom> getKingdomsByRoles(long guildId, List<Long> roleIds);

    Mono<Kingdom> saveKingdom(Kingdom kingdom);

}
