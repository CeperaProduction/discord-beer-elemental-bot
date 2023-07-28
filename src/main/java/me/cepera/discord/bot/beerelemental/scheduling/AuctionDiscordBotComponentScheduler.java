package me.cepera.discord.bot.beerelemental.scheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import me.cepera.discord.bot.beerelemental.discord.DiscordBot;
import me.cepera.discord.bot.beerelemental.discord.components.AuctionDiscordBotComponent;

public class AuctionDiscordBotComponentScheduler implements DiscordBotScheduler{

    private final AuctionDiscordBotComponent auctionComponent;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Inject
    public AuctionDiscordBotComponentScheduler(AuctionDiscordBotComponent auctionComponent) {
        this.auctionComponent = auctionComponent;
    }

    @Override
    public void start(DiscordBot bot) {
        executor.scheduleAtFixedRate(()->auctionComponent.completeEndedAuctions(bot).subscribe(),
                1, 5, TimeUnit.SECONDS);
    }

}
