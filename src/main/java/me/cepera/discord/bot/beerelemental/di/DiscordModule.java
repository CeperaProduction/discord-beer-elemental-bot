package me.cepera.discord.bot.beerelemental.di;

import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import me.cepera.discord.bot.beerelemental.discord.DiscordBot;
import me.cepera.discord.bot.beerelemental.discord.DiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.components.AuctionDiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.components.FamArenaDiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.components.ImageToTextDiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.components.KingdomDataDiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.components.LocaleDiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.components.PermissionsDiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.components.WolfTableDiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.ComplexDiscordBot;
import me.cepera.discord.bot.beerelemental.scheduling.AuctionDiscordBotComponentScheduler;
import me.cepera.discord.bot.beerelemental.scheduling.DiscordBotScheduler;

@Module
public class DiscordModule {

    @Provides
    @Singleton
    DiscordBot discordBot(ComplexDiscordBot bot, Set<DiscordBotScheduler> schedulers) {
        schedulers.forEach(scheduler->scheduler.start(bot));
        return bot;
    }

    @Provides
    @IntoSet
    DiscordBotComponent permissionsDiscordBotComponent(PermissionsDiscordBotComponent component) {
        return component;
    }

    @Provides
    @IntoSet
    DiscordBotComponent localeDiscordBotComponent(LocaleDiscordBotComponent component) {
        return component;
    }

    @Provides
    @IntoSet
    DiscordBotComponent imageToTextDiscordBotComponent(ImageToTextDiscordBotComponent component) {
        return component;
    }

    @Provides
    @IntoSet
    DiscordBotComponent kingdomDataDiscordBotComponent(KingdomDataDiscordBotComponent component) {
        return component;
    }

    @Provides
    @IntoSet
    DiscordBotComponent famArenaDiscordBotComponent(FamArenaDiscordBotComponent component) {
        return component;
    }

    @Provides
    @IntoSet
    DiscordBotComponent wolfTableDiscordBotComponent(WolfTableDiscordBotComponent component) {
        return component;
    }

    @Provides
    @IntoSet
    DiscordBotComponent auctionDiscordBotModule(AuctionDiscordBotComponent component) {
        return component;
    }

    @Provides
    @IntoSet
    DiscordBotScheduler auctionDiscordBotModuleScheduler(AuctionDiscordBotComponentScheduler scheduler) {
        return scheduler;
    }

}
