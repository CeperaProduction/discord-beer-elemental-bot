package me.cepera.discord.bot.beerelemental.converter;

import reactor.core.publisher.Mono;

public interface BodyConverter<T> {

    Mono<byte[]> write(T object);

    Mono<T> read(byte[] bytes);

}
