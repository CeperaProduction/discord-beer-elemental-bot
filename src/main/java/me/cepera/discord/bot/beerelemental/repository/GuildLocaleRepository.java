package me.cepera.discord.bot.beerelemental.repository;

import me.cepera.discord.bot.beerelemental.model.GuildLocale;
import reactor.core.publisher.Mono;

public interface GuildLocaleRepository {

    Mono<Void> setGuildLocale(long guildId, GuildLocale locale);

    Mono<GuildLocale> getGuildLocale(long guildId);

}
