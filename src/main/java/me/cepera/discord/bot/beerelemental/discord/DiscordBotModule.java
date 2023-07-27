package me.cepera.discord.bot.beerelemental.discord;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DiscordBotModule {

    default Flux<ApplicationCommandRequest> commandsToRegister() {
        return Flux.empty();
    }

    default Mono<Void> handleChatInputInteractionEvent(ChatInputInteractionEvent event){
        return Mono.empty();
    }

    default Mono<Void> handleMessageInteractionEvent(MessageInteractionEvent event) {
        return Mono.empty();
    }

}
