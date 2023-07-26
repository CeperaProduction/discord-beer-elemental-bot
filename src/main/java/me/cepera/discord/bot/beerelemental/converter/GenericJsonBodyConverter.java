package me.cepera.discord.bot.beerelemental.converter;

import reactor.core.publisher.Mono;

public class GenericJsonBodyConverter<T> extends AbstractJsonBodyConverter<T> {

    private final Class<T> dataClass;

    public GenericJsonBodyConverter(Class<T> dataClass) {
        this.dataClass = dataClass;
    }

    @Override
    public Mono<T> read(byte[] bytes) {
        return read(dataClass, bytes);
    }

}
