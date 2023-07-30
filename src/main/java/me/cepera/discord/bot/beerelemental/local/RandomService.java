package me.cepera.discord.bot.beerelemental.local;

import java.util.List;

import reactor.core.publisher.Mono;

public interface RandomService {

    Mono<List<Integer>> getRandomIntegers(int min, int max, int maxCount, boolean unique);

}
