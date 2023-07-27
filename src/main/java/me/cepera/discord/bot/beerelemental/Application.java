package me.cepera.discord.bot.beerelemental;

import org.apache.commons.cli.ParseException;

import me.cepera.discord.bot.beerelemental.di.DaggerBaseComponent;

public class Application {

    public static void main(String[] args) throws ParseException {

        DaggerBaseComponent.create().getDiscordBot().start();

    }

}
