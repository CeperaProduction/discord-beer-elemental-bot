package me.cepera.discord.bot.beerelemental.discord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import io.netty.util.internal.ThrowableUtil;
import me.cepera.discord.bot.beerelemental.config.DiscordBotConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ComplexDiscordBot extends BasicDiscordBot{

    private static final Logger LOGGER = LogManager.getLogger(ComplexDiscordBot.class);

    private final List<DiscordBotComponent> components;

    @Inject
    public ComplexDiscordBot(Set<DiscordBotComponent> discordBotComponents, DiscordBotConfig config) {
        super(config);
        this.components = Collections.unmodifiableList(new ArrayList<DiscordBotComponent>(discordBotComponents));
    }

    @Override
    protected Flux<ApplicationCommandRequest> commandsToRegister() {
       return Flux.fromIterable(components).flatMap(DiscordBotComponent::commandsToRegister);
    }

    @Override
    protected Mono<Void> handleChatInputInteractionEvent(ChatInputInteractionEvent event) {
        return handeForEachComponent(event, DiscordBotComponent::handleChatInputInteractionEvent);
    }

    @Override
    protected Mono<Void> handleMessageInteractionEvent(MessageInteractionEvent event) {
        return handeForEachComponent(event, DiscordBotComponent::handleMessageInteractionEvent);
    }

    @Override
    protected Mono<Void> handleChatInputAutocompleteEvent(ChatInputAutoCompleteEvent event) {
        return handeForEachComponent(event, DiscordBotComponent::handleChatInputAutocompleteEvent);
    }

    private <T> Mono<Void> handeForEachComponent(T event, BiFunction<DiscordBotComponent, T, Mono<Void>> componentHandler){
        return Flux.fromIterable(components)
                .flatMap(component->componentHandler.apply(component, event).onErrorResume(e->{
                    LOGGER.error("Unhandled exception in component {}: {}",
                            component.getClass().getName(), ThrowableUtil.stackTraceToString(e));
                    return Mono.empty();
                }))
                .then();
    }

}
