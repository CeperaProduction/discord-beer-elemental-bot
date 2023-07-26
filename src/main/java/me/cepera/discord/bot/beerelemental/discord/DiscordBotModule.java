package me.cepera.discord.bot.beerelemental.discord;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DiscordBotModule {

    Flux<ApplicationCommandRequest> commandsToRegister();

    Mono<Void> handleChatInputInteractionEvent(ChatInputInteractionEvent event);

}
