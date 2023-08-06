package me.cepera.discord.bot.beerelemental.repository;

import me.cepera.discord.bot.beerelemental.model.FamArenaBattle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FamArenaBattleRepository {

    Flux<String> findOpponentNicknames(long guildId);

    Flux<String> findOpponentNicknames(long guildId, String search);

    Flux<FamArenaBattle> findOpponentBattles(long guildId, String opponent, long minTimestamp, int offset, int count, Boolean winOnly);

    Mono<FamArenaBattle> addBattle(FamArenaBattle battle);

}
