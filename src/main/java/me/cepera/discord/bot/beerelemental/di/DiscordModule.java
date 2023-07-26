package me.cepera.discord.bot.beerelemental.di;

import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import me.cepera.discord.bot.beerelemental.discord.DiscordBot;
import me.cepera.discord.bot.beerelemental.discord.DiscordBotModule;
import me.cepera.discord.bot.beerelemental.discord.ModuledDiscordBot;
import me.cepera.discord.bot.beerelemental.discord.modules.AuctionDiscordBotModule;
import me.cepera.discord.bot.beerelemental.discord.modules.LocaleDiscordBotModule;
import me.cepera.discord.bot.beerelemental.scheduling.AuctionDiscordBotModuleScheduler;
import me.cepera.discord.bot.beerelemental.scheduling.DiscordBotScheduler;

@Module
public class DiscordModule {

    @Provides
    @Singleton
    DiscordBot discordBot(ModuledDiscordBot bot, Set<DiscordBotScheduler> schedulers) {
        schedulers.forEach(scheduler->scheduler.start(bot));
        return bot;
    }

    @Provides
    @IntoSet
    DiscordBotModule localeDiscordBotModule(LocaleDiscordBotModule module) {
        return module;
    }

    @Provides
    @IntoSet
    DiscordBotModule auctionDiscordBotModule(AuctionDiscordBotModule module) {
        return module;
    }

    @Provides
    @IntoSet
    DiscordBotScheduler auctionDiscordBotModuleScheduler(AuctionDiscordBotModuleScheduler scheduler) {
        return scheduler;
    }

}
