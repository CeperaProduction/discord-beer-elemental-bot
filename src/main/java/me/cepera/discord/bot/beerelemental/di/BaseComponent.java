package me.cepera.discord.bot.beerelemental.di;

import javax.inject.Singleton;

import dagger.Component;
import me.cepera.discord.bot.beerelemental.discord.DiscordBot;

@Singleton
@Component(modules = {DiscordModule.class, DataModule.class})
public interface BaseComponent {

    DiscordBot getDiscordBot();

}