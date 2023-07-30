package me.cepera.discord.bot.beerelemental.di;

import javax.inject.Singleton;

import dagger.Component;
import me.cepera.discord.bot.beerelemental.discord.DiscordBot;

@Singleton
@Component(modules = {DiscordModule.class, DataModule.class, ConfigModule.class,
        ImageToTextModule.class, RandomModule.class})
public interface BaseComponent {

    DiscordBot getDiscordBot();

}
