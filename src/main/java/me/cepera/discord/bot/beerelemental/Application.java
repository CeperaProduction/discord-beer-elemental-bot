package me.cepera.discord.bot.beerelemental;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import me.cepera.discord.bot.beerelemental.di.DaggerBaseComponent;

public class Application {

    public static final String ENVIRONMENT_DISCORD_BOT_KEY = "DISCORD_BOT_KEY";

    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        Option botKeyOption = new Option("k", "key", true, "Bot key for Discord bot API. "
                + "If not set, then system environment '"+ENVIRONMENT_DISCORD_BOT_KEY+"' will be used.");
        botKeyOption.setArgs(1);
        botKeyOption.setArgName("discord_bot_api_key");
        botKeyOption.setRequired(false);
        botKeyOption.setType(String.class);

        options.addOption(botKeyOption);

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commaLine = commandLineParser.parse(options, args);

        String botKey = commaLine.getOptionValue(botKeyOption, System.getenv(ENVIRONMENT_DISCORD_BOT_KEY));

        DaggerBaseComponent.create().getDiscordBot().start(botKey);

    }

}
