package me.cepera.discord.bot.beerelemental;

import me.cepera.discord.bot.beerelemental.di.DaggerBaseComponent;

public class Application {

    public static void main(String[] args) {

        DaggerBaseComponent.create().getDiscordBot().start();

    }

}
