package me.cepera.discord.bot.beerelemental.di;

import java.nio.file.Paths;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.cepera.discord.bot.beerelemental.repository.ActiveAuctionRepository;
import me.cepera.discord.bot.beerelemental.repository.GuildLocaleRepository;
import me.cepera.discord.bot.beerelemental.repository.SQLiteActiveRepository;
import me.cepera.discord.bot.beerelemental.repository.SQLiteGuildLocaleRepository;

@Module
public class DataModule {

    @Provides
    @Singleton
    ActiveAuctionRepository auctionRepository() {
        return new SQLiteActiveRepository(Paths.get("data", "auctions.sqlite"));
    }

    @Provides
    @Singleton
    GuildLocaleRepository guildLocaleRepository() {
        return new SQLiteGuildLocaleRepository(Paths.get("data", "guild_locales.sqlite"));
    }

}
