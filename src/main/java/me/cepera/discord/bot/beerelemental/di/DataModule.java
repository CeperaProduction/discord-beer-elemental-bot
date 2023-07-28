package me.cepera.discord.bot.beerelemental.di;

import java.nio.file.Paths;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.cepera.discord.bot.beerelemental.repository.ActiveAuctionRepository;
import me.cepera.discord.bot.beerelemental.repository.GuildLocaleRepository;
import me.cepera.discord.bot.beerelemental.repository.KingdomMemberRepository;
import me.cepera.discord.bot.beerelemental.repository.KingdomRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.SQLiteActiveAuctionRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.SQLiteGuildLocaleRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.SQLiteKingdomMemberRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.SQLiteKingdomRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.db.SQLiteDatabase;

@Module
public class DataModule {

    @Provides
    @Singleton
    @Named("auctionDatabase")
    SQLiteDatabase auctionDatabase() {
        return new SQLiteDatabase(Paths.get("data", "auctions.sqlite"));
    }

    @Provides
    @Singleton
    @Named("guildLocaleDatabase")
    SQLiteDatabase guildLocaleDatabase() {
        return new SQLiteDatabase(Paths.get("data", "guild_locales.sqlite"));
    }

    @Provides
    @Singleton
    @Named("kingdomDatabase")
    SQLiteDatabase kingdomDatabase() {
        return new SQLiteDatabase(Paths.get("data", "kingdoms.sqlite"));
    }

    @Provides
    @Singleton
    ActiveAuctionRepository auctionRepository(@Named("auctionDatabase") SQLiteDatabase database) {
        return new SQLiteActiveAuctionRepository(database);
    }

    @Provides
    @Singleton
    GuildLocaleRepository guildLocaleRepository(@Named("guildLocaleDatabase") SQLiteDatabase database) {
        return new SQLiteGuildLocaleRepository(database);
    }

    @Provides
    @Singleton
    KingdomRepository kingdomRepository(@Named("kingdomDatabase") SQLiteDatabase database) {
        return new SQLiteKingdomRepository(database);
    }

    @Provides
    @Singleton
    KingdomMemberRepository kingdomMemberRepository(@Named("kingdomDatabase") SQLiteDatabase database) {
        return new SQLiteKingdomMemberRepository(database);
    }

}
