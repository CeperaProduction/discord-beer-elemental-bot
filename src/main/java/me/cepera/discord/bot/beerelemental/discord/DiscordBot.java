package me.cepera.discord.bot.beerelemental.discord;

import discord4j.core.DiscordClient;

public interface DiscordBot {

    void start(String botApiKey);

    DiscordClient getDiscordClient();

}
