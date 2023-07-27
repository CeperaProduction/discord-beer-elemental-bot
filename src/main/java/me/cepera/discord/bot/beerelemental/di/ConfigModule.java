package me.cepera.discord.bot.beerelemental.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.cepera.discord.bot.beerelemental.config.ConfigReader;
import me.cepera.discord.bot.beerelemental.config.DiscordBotConfig;
import me.cepera.discord.bot.beerelemental.config.OCRConfig;

@Module
public class ConfigModule {

    @Provides
    @Singleton
    ConfigReader configReader() {
        return new ConfigReader();
    }

    @Provides
    @Singleton
    DiscordBotConfig discordBotConfig(ConfigReader reader) {
        return reader.readConfig("discord", DiscordBotConfig.class);
    }

    @Provides
    @Singleton
    OCRConfig ocrConfig(ConfigReader reader) {
        return reader.readConfig("ocr", OCRConfig.class);
    }

}
