package me.cepera.discord.bot.beerelemental.scheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import me.cepera.discord.bot.beerelemental.discord.DiscordBot;
import me.cepera.discord.bot.beerelemental.discord.modules.AuctionDiscordBotModule;

public class AuctionDiscordBotModuleScheduler implements DiscordBotScheduler{

    private final AuctionDiscordBotModule auctionModule;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Inject
    public AuctionDiscordBotModuleScheduler(AuctionDiscordBotModule auctionModule) {
        this.auctionModule = auctionModule;
    }

    @Override
    public void start(DiscordBot bot) {
        executor.scheduleAtFixedRate(()->auctionModule.completeEndedAuctions(bot).subscribe(),
                1, 5, TimeUnit.SECONDS);
    }

}
