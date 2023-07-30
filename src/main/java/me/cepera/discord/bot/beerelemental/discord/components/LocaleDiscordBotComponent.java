package me.cepera.discord.bot.beerelemental.discord.components;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import io.netty.util.internal.ThrowableUtil;
import me.cepera.discord.bot.beerelemental.discord.DiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.DiscordToolset;
import me.cepera.discord.bot.beerelemental.local.PermissionService;
import me.cepera.discord.bot.beerelemental.local.lang.LanguageService;
import me.cepera.discord.bot.beerelemental.model.GuildLocale;
import me.cepera.discord.bot.beerelemental.repository.GuildLocaleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LocaleDiscordBotComponent implements DiscordBotComponent, DiscordToolset {

    private static final Logger LOGGER = LogManager.getLogger(LocaleDiscordBotComponent.class);

    public static final String COMMAND_LOCALE= "locale";
    public static final String COMMAND_OPTION_LOCALE = "locale";

    private final LanguageService languageService;

    private final PermissionService permissionService;

    private final GuildLocaleRepository guildLocaleRepository;

    @Inject
    public LocaleDiscordBotComponent(LanguageService languageService, PermissionService permissionService,
            GuildLocaleRepository guildLocaleRepository) {
        this.languageService = languageService;
        this.permissionService = permissionService;
        this.guildLocaleRepository = guildLocaleRepository;
    }

    @Override
    public LanguageService languageService() {
        return languageService;
    }

    @Override
    public PermissionService permissionService() {
        return permissionService;
    }

    @Override
    public Flux<ApplicationCommandRequest> commandsToRegister() {
        return Flux.create(sink->{
            sink.next(ApplicationCommandRequest.builder()
                    .name(COMMAND_LOCALE)
                    .nameLocalizationsOrNull(localization("command.locale"))
                    .description(localization(null, "command.locale.description"))
                    .descriptionLocalizationsOrNull(localization("command.locale.description"))
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_LOCALE)
                            .nameLocalizationsOrNull(localization("command.locale.option.locale"))
                            .description(localization(null, "command.locale.option.locale.description"))
                            .descriptionLocalizationsOrNull(localization("command.locale.option.locale.description"))
                            .required(true)
                            .type(4)
                            .addAllChoices(Arrays.stream(GuildLocale.values())
                                    .map(locale->ApplicationCommandOptionChoiceData.builder()
                                            .name(locale.name())
                                            .value(locale.index())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build())
                    .build());
        });
    }

    @Override
    public Mono<Void> handleChatInputInteractionEvent(ChatInputInteractionEvent event) {
        if(!getCommandName(event).equals(COMMAND_LOCALE)) {
            return Mono.empty();
        }

        Integer localeIndex = event.getOption(COMMAND_OPTION_LOCALE)
                .flatMap(option->option.getValue())
                .map(value->(int)value.asLong())
                .orElse(null);

        return handleLocaleCommand(event, GuildLocale.fromIndex(localeIndex));

    }

    private Mono<Void> handleLocaleCommand(ChatInputInteractionEvent event, GuildLocale locale){

        String actionIdentity = createActionIdentity(event, "locale");
        return event.getInteraction().getGuild()
                .switchIfEmpty(event.reply()
                        .withContent(onlyForServerResponseText(event))
                        .then(Mono.empty()))
                .flatMap(guild->handlePermissionCheck(guild,
                        Mono.justOrEmpty(event.getInteraction().getMember())
                            .flatMap(member->permissionService().isAdmin(guild, member)),
                        simpleReply(event, defaultOnlyForAdminText(event), true)))
                .flatMap(guild->guildLocaleRepository.setGuildLocale(guild.getId().asLong(), locale)
                        .then(Mono.fromRunnable(()->LOGGER.info("Locale of guild {} set to {}", guild.getId().asLong(), locale)))
                        .then(event.reply()
                                .withContent(guildLocaleChangedResponseText(event, locale))
                                .withEphemeral(false))
                            .onErrorResume(e->{
                                LOGGER.error("Error during server locale set. Action: {} Error: {}", actionIdentity, ThrowableUtil.stackTraceToString(e));
                                return replyError(event, this::defaultCommandErrorText, true);
                            }));

    }

    private String guildLocaleChangedResponseText(ApplicationCommandInteractionEvent event, GuildLocale locale) {
        return localization(event.getInteraction().getUserLocale(), "message.locale.set", "locale", locale.name());
    }

    private String onlyForServerResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.locale.only_in_channel");
    }

}
