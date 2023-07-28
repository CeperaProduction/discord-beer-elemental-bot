package me.cepera.discord.bot.beerelemental.discord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        return Flux.fromIterable(components)
                .flatMap(module->module.handleChatInputInteractionEvent(event).onErrorResume(e->{
                    LOGGER.error("Unhandled exception in component {}: {}", module.getClass().getName(), ThrowableUtil.stackTraceToString(e));
                    return Mono.empty();
                }))
                .then();
    }

    @Override
    protected Mono<Void> handleMessageInteractionEvent(MessageInteractionEvent event) {
        return Flux.fromIterable(components)
                .flatMap(module->module.handleMessageInteractionEvent(event).onErrorResume(e->{
                    LOGGER.error("Unhandled exception in component {}: {}", module.getClass().getName(), ThrowableUtil.stackTraceToString(e));
                    return Mono.empty();
                }))
                .then();
    }

}
