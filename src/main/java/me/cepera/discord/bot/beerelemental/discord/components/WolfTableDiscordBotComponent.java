package me.cepera.discord.bot.beerelemental.discord.components;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.component.TextInput;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateFields;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import io.netty.util.internal.ThrowableUtil;
import me.cepera.discord.bot.beerelemental.discord.DiscordBot;
import me.cepera.discord.bot.beerelemental.discord.DiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.DiscordToolset;
import me.cepera.discord.bot.beerelemental.local.PermissionService;
import me.cepera.discord.bot.beerelemental.local.lang.LanguageService;
import me.cepera.discord.bot.beerelemental.model.Kingdom;
import me.cepera.discord.bot.beerelemental.model.KingdomMember;
import me.cepera.discord.bot.beerelemental.model.Permission;
import me.cepera.discord.bot.beerelemental.repository.KingdomMemberRepository;
import me.cepera.discord.bot.beerelemental.repository.KingdomRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class WolfTableDiscordBotComponent implements DiscordBotComponent, DiscordToolset{

    private static final Logger LOGGER = LogManager.getLogger(WolfTableDiscordBotComponent.class);

    public static final String COMMAND_WOLF= "wolf";
    public static final String COMMAND_WOLF_SUB_TABLE= "table";
    public static final String COMMAND_WOLF_SUB_CANDIDATES= "candidates";
    public static final String COMMAND_WOLF_SUB_RECEIVED= "received";
    public static final String COMMAND_WOLF_OPTION_KINGDOM = "kingdom";
    public static final String COMMAND_WOLF_OPTION_NICKNAMES = "nicknames";
    public static final String COMMAND_WOLF_OPTION_NICKNAME = "nickname";
    public static final String COMMAND_WOLF_OPTION_NEW_CYCLE = "new_cycle";
    public static final String COMMAND_WOLF_OPTION_MAX_PENALTY= "max_penalty";

    private final LanguageService languageService;

    private final PermissionService permissionService;

    private final KingdomRepository kingdomRepository;

    private final KingdomMemberRepository memberRepository;

    private final Map<String, WolfEditModalContext> wolfEditContexts = new ConcurrentHashMap<>();

    private final Pattern wolfInputPattern = Pattern.compile("(?<stars>[3-6])\\*\\s*(\\+?(?<add>\\d))?");

    @Inject
    public WolfTableDiscordBotComponent(LanguageService languageService, PermissionService permissionService,
            KingdomRepository kingdomRepository, KingdomMemberRepository memberRepository) {
        this.languageService = languageService;
        this.permissionService = permissionService;
        this.kingdomRepository = kingdomRepository;
        this.memberRepository = memberRepository;
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
                    .name(COMMAND_WOLF)
                    .nameLocalizationsOrNull(localization("command.wolf"))
                    .description(localization(null, "command.wolf.description"))
                    .descriptionLocalizationsOrNull(localization("command.wolf.description"))

                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_WOLF_SUB_TABLE)
                            .nameLocalizationsOrNull(localization("command.wolf.sub.table"))
                            .description(localization(null, "command.wolf.sub.table.description"))
                            .descriptionLocalizationsOrNull(localization("command.wolf.sub.table.description"))
                            .type(1)
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_WOLF_OPTION_KINGDOM)
                                    .nameLocalizationsOrNull(localization("command.wolf.option.kingdom"))
                                    .description(localization(null, "command.wolf.option.kingdom.description"))
                                    .descriptionLocalizationsOrNull(localization("command.wolf.option.kingdom.description"))
                                    .required(true)
                                    .autocomplete(true)
                                    .type(3)
                                    .build())
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_WOLF_OPTION_NICKNAME)
                                    .nameLocalizationsOrNull(localization("command.wolf.option.nickname"))
                                    .description(localization(null, "command.wolf.option.nickname.description"))
                                    .descriptionLocalizationsOrNull(localization("command.wolf.option.nickname.description"))
                                    .autocomplete(true)
                                    .type(3)
                                    .build())
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_WOLF_OPTION_NEW_CYCLE)
                                    .nameLocalizationsOrNull(localization("command.wolf.option.new_cycle"))
                                    .description(localization(null, "command.wolf.option.new_cycle.description"))
                                    .descriptionLocalizationsOrNull(localization("command.wolf.option.new_cycle.description"))
                                    .required(false)
                                    .type(5)
                                    .build())
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_WOLF_OPTION_MAX_PENALTY)
                                    .nameLocalizationsOrNull(localization("command.wolf.option.max_penalty"))
                                    .description(localization(null, "command.wolf.option.max_penalty.description"))
                                    .descriptionLocalizationsOrNull(localization("command.wolf.option.max_penalty.description"))
                                    .required(false)
                                    .minValue(0.0D)
                                    .maxValue(100.0D)
                                    .type(4)
                                    .build())
                            .build())

                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_WOLF_SUB_CANDIDATES)
                            .nameLocalizationsOrNull(localization("command.wolf.sub.candidates"))
                            .description(localization(null, "command.wolf.sub.candidates.description"))
                            .descriptionLocalizationsOrNull(localization("command.wolf.sub.candidates.description"))
                            .type(1)
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_WOLF_OPTION_KINGDOM)
                                    .nameLocalizationsOrNull(localization("command.wolf.option.kingdom"))
                                    .description(localization(null, "command.wolf.option.kingdom.description"))
                                    .descriptionLocalizationsOrNull(localization("command.wolf.option.kingdom.description"))
                                    .required(true)
                                    .autocomplete(true)
                                    .type(3)
                                    .build())
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_WOLF_OPTION_NICKNAMES)
                                    .nameLocalizationsOrNull(localization("command.wolf.option.nicknames"))
                                    .description(localization(null, "command.wolf.option.nicknames.description.came"))
                                    .descriptionLocalizationsOrNull(localization("command.wolf.option.nicknames.description.came"))
                                    .required(true)
                                    .type(3)
                                    .build())
                            .build())

                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_WOLF_SUB_RECEIVED)
                            .nameLocalizationsOrNull(localization("command.wolf.sub.received"))
                            .description(localization(null, "command.wolf.sub.received.description"))
                            .descriptionLocalizationsOrNull(localization("command.wolf.sub.received.description"))
                            .type(1)
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_WOLF_OPTION_KINGDOM)
                                    .nameLocalizationsOrNull(localization("command.wolf.option.kingdom"))
                                    .description(localization(null, "command.wolf.option.kingdom.description"))
                                    .descriptionLocalizationsOrNull(localization("command.wolf.option.kingdom.description"))
                                    .required(true)
                                    .autocomplete(true)
                                    .type(3)
                                    .build())
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_WOLF_OPTION_NICKNAMES)
                                    .nameLocalizationsOrNull(localization("command.wolf.option.nicknames"))
                                    .description(localization(null, "command.wolf.option.nicknames.description.received"))
                                    .descriptionLocalizationsOrNull(localization("command.wolf.option.nicknames.description.received"))
                                    .required(true)
                                    .type(3)
                                    .build())
                            .build())
                    .build());

        });
    }

    @Override
    public void configureGatewayClient(GatewayDiscordClient client) {

        client.on(ModalSubmitInteractionEvent.class)
            .flatMap(event->this.handleModalSubmitInteractionEvent(event)
                    .onErrorResume(e->{
                        LOGGER.error("Error on handling modal submit interaction", e);
                        return Mono.empty();
                    }))
            .subscribe();

        client.on(ButtonInteractionEvent.class)
            .flatMap(event->this.handleButtonInteractionEvent(event)
                    .onErrorResume(e->{
                        LOGGER.error("Error on handling button interaction", e);
                        return Mono.empty();
                    }))
            .subscribe();

    }

    @Override
    public Mono<Void> handleChatInputInteractionEvent(ChatInputInteractionEvent event, DiscordBot bot) {
        if(!getCommandName(event).equals(COMMAND_WOLF)) {
            return Mono.empty();
        }

        Optional<ApplicationCommandInteractionOption> optSubCommand = getSubCommand(event);

        if(!optSubCommand.isPresent()) {
            return Mono.empty();
        }

        ApplicationCommandInteractionOption subCommand = optSubCommand.get();

        switch(subCommand.getName()) {
        case COMMAND_WOLF_SUB_TABLE: {

            String kingdom = subCommand.getOption(COMMAND_WOLF_OPTION_KINGDOM)
                    .flatMap(option->option.getValue().map(value->value.asString()))
                    .orElse("");

            Optional<String> maybeNickname = subCommand.getOption(COMMAND_WOLF_OPTION_NICKNAME)
                    .flatMap(option->option.getValue().map(value->value.asString()));

            Optional<Boolean> maybeNewCycle = subCommand.getOption(COMMAND_WOLF_OPTION_NEW_CYCLE)
                    .flatMap(option->option.getValue().map(value->value.asBoolean()));

            Optional<Byte> maybeMaxPenalty = subCommand.getOption(COMMAND_WOLF_OPTION_MAX_PENALTY)
                    .flatMap(option->option.getValue().map(value->(byte)value.asLong()));

            return handleTableCommand(event, kingdom, maybeNickname, maybeNewCycle, maybeMaxPenalty);
        }
        case COMMAND_WOLF_SUB_CANDIDATES: {

            String kingdom = subCommand.getOption(COMMAND_WOLF_OPTION_KINGDOM)
                    .flatMap(option->option.getValue().map(value->value.asString()))
                    .orElse("");

            String nicknames = subCommand.getOption(COMMAND_WOLF_OPTION_NICKNAMES)
                    .flatMap(option->option.getValue().map(value->value.asString()))
                    .orElse("");

            return handleCandidatesCommand(event, kingdom, nicknames);
        }
        case COMMAND_WOLF_SUB_RECEIVED: {

            String kingdom = subCommand.getOption(COMMAND_WOLF_OPTION_KINGDOM)
                    .flatMap(option->option.getValue().map(value->value.asString()))
                    .orElse("");

            String nicknames = subCommand.getOption(COMMAND_WOLF_OPTION_NICKNAMES)
                    .flatMap(option->option.getValue().map(value->value.asString()))
                    .orElse("");

            return handleReceivedCommand(event, kingdom, nicknames);

        }
        default:
            return Mono.empty();
        }
    }

    @Override
    public Mono<Void> handleChatInputAutocompleteEvent(ChatInputAutoCompleteEvent event, DiscordBot bot) {
        if(!getCommandName(event).equals(COMMAND_WOLF)) {
            return Mono.empty();
        }
        if(event.getFocusedOption().getName().equals(COMMAND_WOLF_OPTION_KINGDOM)) {
            return event.getInteraction().getGuild()
                    .flatMap(guild->handleKingdomAutocomplete(event, guild));
        }

        Optional<ApplicationCommandInteractionOption> optSubCommand = getSubCommand(event);

        if(event.getFocusedOption().getName().equals(COMMAND_WOLF_OPTION_NICKNAME)) {
            return event.getInteraction().getGuild()
                    .flatMap(guild->handleKingdomMemberAutocomplete(event, guild, optSubCommand.map(sub->sub.getOptions()).orElseGet(()->event.getOptions()).stream()
                                .filter(opt->opt.getName().equals(COMMAND_WOLF_OPTION_KINGDOM))
                                .findAny()));
        }
        return Mono.empty();
    }

    private Mono<Void> handleModalSubmitInteractionEvent(ModalSubmitInteractionEvent event){
        WolfEditModalContext context = wolfEditContexts.remove(event.getCustomId());
        if(context == null) {
            return event.reply(actionExpiredResponseText(event));
        }
        return editPlayerTable(event, context);
    }

    public Mono<Void> handleButtonInteractionEvent(ButtonInteractionEvent event){

        if(!event.getCustomId().equals("wolf.table.print")) {
            return Mono.empty();
        }

        return event.deferEdit()
                .withEphemeral(true)
                .then(Mono.justOrEmpty(event.getMessage()))
                .zipWhen(message->Mono.justOrEmpty(message.getAttachments().stream()
                        .filter(atm->atm.getFilename().endsWith("_wolf_table.txt"))
                        .findAny()))
                .flatMap(tuple->sendTableInMessages(event, tuple.getT1(), tuple.getT2()));

    }

    private Mono<Void> sendTableInMessages(ButtonInteractionEvent event, Message message, Attachment txt){
        return getAttachmentContent(txt, false)
                .map(bytes->new String(bytes, StandardCharsets.UTF_8))
                .flatMapIterable(text->Arrays.asList(text.split("\\n")))
                .buffer(20)
                .flatMap(lines->event.createFollowup()
                        .withEphemeral(true)
                        .withContent("```"+String.join("\n", lines)+"```"))
                .then();
    }

    private Mono<Void> handleKingdomAutocomplete(ChatInputAutoCompleteEvent event, Guild guild){

        return kingdomRepository.getKingdoms(guild.getId().asLong())
                .map(Kingdom::getName)
                .filter(name->event.getFocusedOption().getValue()
                        .map(val->name.toLowerCase().startsWith(val.asString().toLowerCase()))
                        .orElse(true))
                .take(25)
                .map(name->ApplicationCommandOptionChoiceData.builder().name(name).value(name).build())
                .cast(ApplicationCommandOptionChoiceData.class)
                .collectList()
                .flatMap(choices->event.respondWithSuggestions(choices));

    }

    private Mono<Void> handleKingdomMemberAutocomplete(ChatInputAutoCompleteEvent event, Guild guild,
            Optional<ApplicationCommandInteractionOption> kingdomOption){

        return Mono.justOrEmpty(kingdomOption)
                .flatMap(opt->Mono.justOrEmpty(opt.getValue().map(val->val.asString())))
                .flatMap(kingdomName->kingdomRepository.getKingdomByName(guild.getId().asLong(), kingdomName))
                .flatMapMany(memberRepository::getMembers)
                .map(KingdomMember::getName)
                .filter(name->event.getFocusedOption().getValue()
                        .map(val->name.toLowerCase().startsWith(val.asString().toLowerCase()))
                        .orElse(true))
                .take(25)
                .map(name->ApplicationCommandOptionChoiceData.builder().name(name).value(name).build())
                .cast(ApplicationCommandOptionChoiceData.class)
                .collectList()
                .flatMap(choices->event.respondWithSuggestions(choices));

    }

    private Mono<Void> handleTableCommand(ChatInputInteractionEvent event, String kingdom,
            Optional<String> maybeNickname, Optional<Boolean> maybeNewCycle, Optional<Byte> maybeMaxPenalty){

        String actionIdentity = createActionIdentity(event, "wolf.table");

        if(maybeNickname.isPresent()) {
            if(maybeNewCycle.isPresent() || maybeMaxPenalty.isPresent()) {
                return simpleReply(event, singleManageParameterRequiredResponseText(event));
            }
            return event.getInteraction().getGuild()
                    .flatMap(guild->handlePermissionCheck(guild,
                            hasPermission(event.getInteraction(), guild, Permission.WOLF_TABLE_MANAGE),
                            simpleReply(event, defaultNoPermissionText(event))))
                    .switchIfEmpty(simpleReply(event, onlyForServerResponseText(event), false))
                    .zipWhen(guild->kingdomRepository.getKingdomByName(guild.getId().asLong(), kingdom)
                            .switchIfEmpty(simpleReply(event, kingdomNotFoundResponseText(event), false)))
                    .flatMap(tuple->{
                        Guild guild = tuple.getT1();
                        Kingdom kd = tuple.getT2();
                        return editPlayerTable(event, guild, kd, maybeNickname.get(), actionIdentity);
                    })
                    .onErrorResume(e->{
                        LOGGER.error("Error while executing wolf.table command. Action: {} Error: {}",
                                actionIdentity, ThrowableUtil.stackTraceToString(e));
                        return simpleReply(event, defaultCommandErrorText(event), false);
                    });
        }

        return event.deferReply()
                .then(event.getInteraction().getGuild())
                .switchIfEmpty(simpleEditReply(event, onlyForServerResponseText(event)))
                .zipWhen(guild->kingdomRepository.getKingdomByName(guild.getId().asLong(), kingdom)
                        .switchIfEmpty(simpleEditReply(event, kingdomNotFoundResponseText(event))))
                .flatMap(tuple->{
                    Guild guild = tuple.getT1();
                    Kingdom kd = tuple.getT2();
                    if(!maybeNickname.isPresent() && !maybeNewCycle.isPresent() && !maybeMaxPenalty.isPresent()) {
                        return permissionCheckMap(event, guild, Permission.WOLF_TABLE_SHOW)
                                .flatMap(g->sendTable(event, guild, kd));
                    }
                    if(maybeNewCycle.isPresent()) {
                        if(maybeMaxPenalty.isPresent()) {
                            return simpleEditReply(event, singleManageParameterRequiredResponseText(event));
                        }
                        return permissionCheckMap(event, guild, Permission.WOLF_TABLE_MANAGE)
                                .flatMap(g->setNewCycle(event, guild, kd, maybeNewCycle.get()));
                    }
                    if(maybeMaxPenalty.isPresent()) {
                        return permissionCheckMap(event, guild, Permission.WOLF_TABLE_MANAGE)
                                .flatMap(g->setMaxPenalty(event, guild, kd, maybeMaxPenalty.get()));
                    }
                    return simpleEditReply(event, singleManageParameterRequiredResponseText(event));
                })
                .onErrorResume(e->{
                    LOGGER.error("Error while executing wolf.table command. Action: {} Error: {}",
                            actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return simpleEditReply(event, defaultCommandErrorText(event));
                });
    }

    private Mono<Void> handleCandidatesCommand(ChatInputInteractionEvent event, String kingdom, String nicknames){

        String actionIdentity = createActionIdentity(event, "wolf.candidates");

        return event.deferReply()
                .then(event.getInteraction().getGuild())
                .switchIfEmpty(simpleEditReply(event, onlyForServerResponseText(event)))
                .flatMap(guild->permissionCheckMap(event, guild, Permission.WOLF_TABLE_MANAGE))
                .zipWhen(guild->kingdomRepository.getKingdomByName(guild.getId().asLong(), kingdom)
                        .switchIfEmpty(simpleEditReply(event, kingdomNotFoundResponseText(event))))
                .flatMap(tuple->sendCandidates(event, tuple.getT2(), splitNicknames(nicknames)))
                .onErrorResume(e->{
                    LOGGER.error("Error while executing wolf.candidates command. Action: {} Error: {}",
                            actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return simpleEditReply(event, defaultCommandErrorText(event));
                });
    }

    private Mono<Void> handleReceivedCommand(ChatInputInteractionEvent event, String kingdom, String nicknames){

        String actionIdentity = createActionIdentity(event, "wolf.received");

        return event.deferReply()
                .then(event.getInteraction().getGuild())
                .flatMap(guild->permissionCheckMap(event, guild, Permission.WOLF_TABLE_MANAGE))
                .switchIfEmpty(simpleEditReply(event, onlyForServerResponseText(event)))
                .zipWhen(guild->kingdomRepository.getKingdomByName(guild.getId().asLong(), kingdom)
                        .switchIfEmpty(simpleEditReply(event, kingdomNotFoundResponseText(event))))
                .flatMap(tuple->setReceived(event, tuple.getT2(), splitNicknames(nicknames)))
                .onErrorResume(e->{
                    LOGGER.error("Error while executing wolf.received command. Action: {} Error: {}",
                            actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return simpleEditReply(event, defaultCommandErrorText(event));
                });
    }

    private Mono<Void> sendTable(ChatInputInteractionEvent event, Guild guild, Kingdom kingdom){
        return memberRepository.getMembers(kingdom)
                .collectList()
                .map(members->printTable(members, true))
                .flatMap(table->{
                    String text = tableResponseText(event, kingdom);
                    table = "#   "+tableHeaderText(event)+"\n"+table;
                    MessageCreateFields.File file = MessageCreateFields.File.of(kingdom.getName().toLowerCase()+"_wolf_table.txt",
                            new ByteArrayInputStream(table.getBytes(StandardCharsets.UTF_8)));
                    return event.editReply()
                            .withContentOrNull(text)
                            .withComponents(ActionRow.of(Button.secondary("wolf.table.print", ReactionEmoji.of(null, "\uD83D\uDCDD", false))))
                            .withFiles(file);
                })
                .then();
    }

    private String printTable(List<KingdomMember> members, boolean withNumber) {
        String tablePattern = "%-12s %-6s %s      %-6s";
        List<String> lines = new LinkedList<>();
        IntStream.range(1, members.size()+1)
            .forEach(number->{
                KingdomMember member = members.get(number-1);
                if(withNumber) {
                    lines.add(String.format("%-3s "+tablePattern,
                            number,
                            member.getName(),
                            wolfsToString(member.getWolfData().getWolfs()),
                            receivedToString(member.getWolfData().isReceived()),
                            penaltiesToString(member.getWolfData().getPenalty())));
                }else {
                    lines.add(String.format(tablePattern,
                            member.getName(),
                            wolfsToString(member.getWolfData().getWolfs()),
                            receivedToString(member.getWolfData().isReceived()),
                            penaltiesToString(member.getWolfData().getPenalty())));
                }
            });
        return String.join("\n", lines);
    }

    private String wolfsToString(int wolfs) {
        if(wolfs < 1) {
            return "-\u2605";
        }
        switch(wolfs) {
        case 1: return "3\u2605";
        case 2: return "4\u2605";
        case 3: return "4\u2605 +1";
        case 4: return "5\u2605";
        case 5: return "5\u2605 +1";
        case 6: return "5\u2605 +2";
        default: return "6\u2605";
        }
    }

    private String wolfsToStringSimple(int wolfs) {
        if(wolfs < 1) {
            return "0";
        }
        switch(wolfs) {
        case 1: return "3*";
        case 2: return "4*";
        case 3: return "4* +1";
        case 4: return "5*";
        case 5: return "5* +1";
        case 6: return "5* +2";
        default: return "6*";
        }
    }

    private String penaltiesToString(int penalties) {
        if(penalties < 1) {
            return "";
        }
        switch(penalties) {
        case 1: return "\u274C";
        case 2: return "\u274C\u274C";
        case 3: return "\u274C\u274C\u274C";
        default: return penalties+"x\u274C";
        }
    }

    private String receivedToString(boolean received) {
        if(received) {
            return "\u2705";
        }else {
            return "\u2B1B";
        }
    }

    private Mono<Void> editPlayerTable(ChatInputInteractionEvent event, Guild guild, Kingdom kingdom, String nickname, String actionIdentity){
        return memberRepository.findMemberByNickname(kingdom, nickname)
                .switchIfEmpty(simpleReply(event, memberNotFoundResponseText(event), false))
                .flatMap(member->{
                    WolfEditModalContext context = createWolfEditModalContext(kingdom, member, actionIdentity);
                    List<LayoutComponent> components = new LinkedList<>();
                    /*
                    components.add(ActionRow.of(SelectMenu.of("wolf.count", IntStream.range(0, 8)
                            .mapToObj(wolfs->{
                                String stars = wolfsToString(wolfs);
                                return SelectMenu.Option.of(stars, Integer.toString(wolfs))
                                        .withDefault(member.getWolfData().getWolfs() == wolfs);
                            })
                            .collect(Collectors.toList()))
                            .withPlaceholder(modalWolfs(event))
                            .withMinValues(1)
                            .withMaxValues(1)));*/
                    components.add(ActionRow.of(TextInput.small("wolf.count", modalWolfs(event), 1, 5)
                            .prefilled(wolfsToStringSimple(member.getWolfData().getWolfs()))
                            .required()
                            .placeholder("Example: 4* +1")));
                    components.add(ActionRow.of(TextInput.small("wolf.penalties", modalPenalties(event), 1, 2)
                            .prefilled(Byte.toString(member.getWolfData().getPenalty()))
                            .required()
                            .placeholder("0-99")));
                    components.add(ActionRow.of(TextInput.small("wolf.received", modalReceived(event), 1, 1)
                            .prefilled(member.getWolfData().isReceived() ? "+" : "-")
                            .required()
                            .placeholder("+/-")));
                    /*
                    components.add(ActionRow.of(SelectMenu.of("wolf.received",
                            SelectMenu.Option.of(modalReceivedValue(event, true), "true").withDefault(member.getWolfData().isReceived()),
                            SelectMenu.Option.of(modalReceivedValue(event, false), "false").withDefault(!member.getWolfData().isReceived()))
                            .withPlaceholder(modalReceived(event))
                            .withMinValues(1)
                            .withMaxValues(1)));
                            */
                    return event.presentModal()
                            .withCustomId(context.id)
                            .withTitle(modalTitle(event, kingdom, member))
                            .withComponents(components)
                            .withEvent(event);
                });
    }

    private Mono<Void> editPlayerTable(ModalSubmitInteractionEvent event, WolfEditModalContext context){
        return event.deferReply()
                .then(kingdomRepository.getKingdom(context.kingdomId)
                        .switchIfEmpty(simpleEditReply(event, kingdomNotFoundResponseText(event))))
                .flatMap(kingdom->memberRepository.findMemberByNickname(kingdom, context.memberName)
                        .switchIfEmpty(simpleEditReply(event, memberNotFoundResponseText(event)))
                        .flatMap(member->{
                            Optional<Byte> optWolfs = getText(event, "wolf.count").flatMap(this::parseWolf);
                            Optional<Byte> optPenalties = getText(event, "wolf.penalties").flatMap(this::parseByte);
                            Optional<Boolean> optReceived = getText(event, "wolf.received").flatMap(this::parseBoolean);
                            optWolfs.ifPresent(member.getWolfData()::setWolfs);
                            optPenalties.ifPresent(member.getWolfData()::setPenalty);
                            optReceived.ifPresent(member.getWolfData()::setReceived);
                            String responseText = tableEditedResponseText(event, kingdom, member);
                            responseText += "\n```"
                                    +tableHeaderText(event)+"\n"
                                    +printTable(Arrays.asList(member), false)+"```";
                            return memberRepository.saveMember(member)
                                    .then(simpleEditReply(event, responseText))
                                    .then(Mono.fromRunnable(()->LOGGER.info("Kingdom member's with id {} wolf data was changed by action {}",
                                            member.getId(), context.actionIdentity)));
                        }))
                .onErrorResume(e->{
                    LOGGER.error("Error while wolf.title modal window processing. Action: {} Error: {}",
                            context.actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return simpleEditReply(event, defaultCommandErrorText(event));
                })
                .then();
    }

    /*
    private Optional<String> getSelected(ModalSubmitInteractionEvent event, String selectorId){
        return event.getComponents(SelectMenu.class).stream()
                .filter(component->component.getCustomId().equals(selectorId))
                .findAny()
                .flatMap(component->component.getValues())
                .filter(selected->!selected.isEmpty())
                .map(selected->selected.get(0));
    }
    */

    private Optional<String> getText(ModalSubmitInteractionEvent event, String textInputId){
        return event.getComponents(TextInput.class).stream()
                .filter(component->component.getCustomId().equals(textInputId))
                .findAny()
                .flatMap(component->component.getValue())
                .filter(value->!value.isEmpty());
    }

    private Mono<Void> setReceived(ChatInputInteractionEvent event, Kingdom kingdom, List<String> nicknames){
        List<String> unknownNicknames = new ArrayList<String>();
        return memberRepository.getMembers(kingdom)
                .collectMap(member->member.getName().toLowerCase())
                .flatMapMany(membersMap->Flux.fromIterable(nicknames)
                        .flatMap(nickname->{
                            KingdomMember member = membersMap.get(nickname.toLowerCase());
                            if(member == null) {
                                unknownNicknames.add(nickname);
                                return Mono.empty();
                            }else {
                                return Mono.just(member);
                            }
                        }))
                .flatMap(member->{
                    member.getWolfData().setReceived(true);
                    member.getWolfData().setWolfs((byte) Math.min(member.getWolfData().getWolfs()+1, 7));
                    return memberRepository.saveMember(member).then(Mono.fromSupplier(member::getName));
                })
                .collectList()
                .flatMap(receivedNicknames->simpleEditReply(event, wolfReceivedResponseText(event, kingdom, receivedNicknames, unknownNicknames)));
    }

    private Mono<Void> sendCandidates(ChatInputInteractionEvent event, Kingdom kingdom, List<String> nicknames){
        Set<String> lowerNicknames = new HashSet<>();
        nicknames.forEach(nick->lowerNicknames.add(nick.toLowerCase()));
        List<String> unknownNicknames = new LinkedList<>();
        List<String> notNeeded = new LinkedList<>();
        List<String> penaltied = new LinkedList<>();
        List<String> received = new LinkedList<>();
        return memberRepository.getMembers(kingdom)
                .collectList()
                .zipWhen(list->Flux.fromIterable(list)
                        .collectMap(member->member.getName().toLowerCase()))
                .flatMap(tuple->Flux.fromIterable(tuple.getT1())
                        .filter(member->!lowerNicknames.contains(member.getName().toLowerCase()))
                        .collectList()
                        .map(notCame->Tuples.of(tuple.getT1(), tuple.getT2(), notCame)))
                .flatMap(tuple->Flux.fromIterable(nicknames)
                        .flatMap(nickname->{
                            KingdomMember member = tuple.getT2().get(nickname.toLowerCase());
                            if(member == null) {
                                unknownNicknames.add(nickname);
                                return Mono.empty();
                            }else {
                                byte penalty = member.getWolfData().getPenalty();
                                member.getWolfData().setPenalty((byte) Math.max(penalty-1, 0));
                                if(member.getWolfData().getPenalty() != penalty) {
                                    return memberRepository.saveMember(member).then(Mono.just(member));
                                }else {
                                    return Mono.just(member);
                                }
                            }
                        })
                        .collectList()
                        .zipWith(Flux.fromIterable(tuple.getT3())
                                .flatMap(member->{
                                    byte penalty = member.getWolfData().getPenalty();
                                    if(penalty < kingdom.getWolfMaxPenalty()) {
                                        member.getWolfData().setPenalty((byte) (penalty+1));
                                        return memberRepository.saveMember(member).then(Mono.just(member));
                                    }else {
                                        return Mono.just(member);
                                    }
                                })
                                .collectList()))
                .flatMap(tuple->Mono.zip(Flux.fromIterable(tuple.getT1())
                            .filter(member->{
                                if(member.getWolfData().getWolfs() > 6) {
                                    notNeeded.add(member.getName());
                                    return false;
                                }
                                if(member.getWolfData().getPenalty() > 0) {
                                    penaltied.add(member.getName());
                                    return false;
                                }
                                if(member.getWolfData().isReceived()) {
                                    received.add(member.getName());
                                    return false;
                                }
                                return true;
                            })
                            .map(KingdomMember::getName)
                            .collectList(),
                        Flux.fromIterable(tuple.getT2())
                            .map(KingdomMember::getName)
                            .collectList()))
                .flatMap(visitedAndMissed->simpleEditReply(event, wolfCandidatesResponseText(event, kingdom,
                        visitedAndMissed.getT1(), visitedAndMissed.getT2(), notNeeded, penaltied, received, unknownNicknames)));
    }

    private Optional<Byte> parseByte(String strVal){
        try {
            return Optional.of(Byte.parseByte(strVal));
        }catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Byte> parseWolf(String strVal){
        Matcher matcher = wolfInputPattern.matcher(strVal);
        if(!matcher.matches()) {
            return Optional.empty();
        }
        String starsStr = matcher.group("stars");
        String addStr = matcher.group("add");
        switch (starsStr) {
        case "0":
            return Optional.of((byte)0);
        case "3":
            return Optional.of((byte)1);
        case "4":
            if("1".equals(addStr)) {
                return Optional.of((byte)3);
            }
            return Optional.of((byte)2);
        case "5":
            if("1".equals(addStr)) {
                return Optional.of((byte)5);
            }
            if("2".equals(addStr)) {
                return Optional.of((byte)6);
            }
            return Optional.of((byte)4);
        case "6":
            return Optional.of((byte)7);
        default:
            return Optional.empty();
        }
    }

    private Optional<Boolean> parseBoolean(String strVal){
        if("true".equalsIgnoreCase(strVal) || "+".equalsIgnoreCase(strVal) || "1".equalsIgnoreCase(strVal)) {
            return Optional.of(true);
        }
        if("false".equalsIgnoreCase(strVal) || "-".equalsIgnoreCase(strVal) || "0".equalsIgnoreCase(strVal)) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    private Mono<Void> setNewCycle(ChatInputInteractionEvent event, Guild guild, Kingdom kingdom, boolean value){
        if(!value) {
            return event.editReply().then();
        }
        return memberRepository.dropReceivedWolfs(kingdom)
                .then(simpleEditReply(event, newCycleResponseText(event, kingdom)))
                .then();
    }

    private Mono<Void> setMaxPenalty(ChatInputInteractionEvent event, Guild guild, Kingdom kingdom, byte value){
        value = (byte) Math.max(Math.min(value, 100), 0);
        kingdom.setWolfMaxPenalty(value);
        return kingdomRepository.saveKingdom(kingdom)
                .then(simpleEditReply(event, maxPenaltySetResponseText(event, kingdom, value)));
    }

    private Mono<Guild> permissionCheckMap(DeferrableInteractionEvent event, Guild guild, Permission permission){
        return handlePermissionCheck(guild,
                hasPermission(event.getInteraction(), guild, permission),
                simpleEditReply(event, defaultNoPermissionText(event)));
    }

    private List<String> splitNicknames(String rawNicknames){
        List<String> memberNameList = Arrays.stream(rawNicknames.split("\\s"))
                .map(String::trim).filter(str->!str.isEmpty()).collect(Collectors.toList());
        return new ArrayList<>(new LinkedHashSet<>(memberNameList));
    }

    private String modalTitle(DeferrableInteractionEvent event, Kingdom kingdom, KingdomMember member) {
        return localization(event.getInteraction().getUserLocale(), "modal.wolf.table.title", "kingdom", kingdom.getName(), "member", member.getName());
    }

    private String modalWolfs(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "modal.wolf.table.wolfs");
    }

    private String modalPenalties(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "modal.wolf.table.penalties");
    }

    private String modalReceived(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "modal.wolf.table.received");
    }

    /*
    private String modalReceivedValue(DeferrableInteractionEvent event, boolean value) {
        return localization(event.getInteraction().getUserLocale(), "modal.wolf.table.received."+(value ? "yes" : "no"));
    }
    */

    private String tableResponseText(DeferrableInteractionEvent event, Kingdom kingdom) {
        return localization(event.getInteraction().getUserLocale(), "message.wolf.table", "kingdom", kingdom.getName());
    }

    private String tableHeaderText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.wolf.table.header");
    }

    private String newCycleResponseText(DeferrableInteractionEvent event, Kingdom kingdom) {
        return localization(event.getInteraction().getUserLocale(), "message.wolf.new_cycle", "kingdom", kingdom.getName());
    }

    private String maxPenaltySetResponseText(DeferrableInteractionEvent event, Kingdom kingdom, int value) {
        return localization(event.getInteraction().getUserLocale(), "message.wolf.max_penalty_set", "kingdom", kingdom.getName(), "value", Integer.toString(value));
    }

    private String tableEditedResponseText(DeferrableInteractionEvent event, Kingdom kingdom, KingdomMember member) {
        return localization(event.getInteraction().getUserLocale(), "message.wolf.table.edited", "kingdom", kingdom.getName(), "member", member.getName());
    }

    private String wolfReceivedResponseText(DeferrableInteractionEvent event, Kingdom kingdom, List<String> receivedNicknames, List<String> unknownNicknames) {
        String text = localization(event.getInteraction().getUserLocale(), "message.wolf.received", "kingdom", kingdom.getName(), "nicknames", String.join(" ", receivedNicknames));
        if(!unknownNicknames.isEmpty()) {
            text += "\n" + localization(event.getInteraction().getUserLocale(), "message.wolf.unknown_nicknames", "nicknames", String.join(" ", unknownNicknames));
        }
        return text;
    }

    private String wolfCandidatesResponseText(DeferrableInteractionEvent event, Kingdom kingdom,
            List<String> candidates, List<String> missed, List<String> notNeeded, List<String> blocked, List<String> received, List<String> unknown) {

        List<String> textBlocks = new LinkedList<String>();

        if(!unknown.isEmpty()) {
            textBlocks.add(localization(event.getInteraction().getUserLocale(), "message.wolf.unknown_nicknames", "nicknames", String.join(" ", unknown)));
        }

        if(!missed.isEmpty()) {
            textBlocks.add(localization(event.getInteraction().getUserLocale(), "message.wolf.candidates.missed", "nicknames", String.join(" ", missed)));
        }

        if(!notNeeded.isEmpty()) {
            textBlocks.add(localization(event.getInteraction().getUserLocale(), "message.wolf.candidates.not_need", "nicknames", String.join(" ", notNeeded)));
        }

        if(!blocked.isEmpty()) {
            textBlocks.add(localization(event.getInteraction().getUserLocale(), "message.wolf.candidates.blocked", "nicknames", String.join(" ", blocked)));
        }

        if(!received.isEmpty()) {
            textBlocks.add(localization(event.getInteraction().getUserLocale(), "message.wolf.candidates.received", "nicknames", String.join(" ", received)));
        }

        if(!candidates.isEmpty()) {
            textBlocks.add(localization(event.getInteraction().getUserLocale(), "message.wolf.candidates", "nicknames", String.join(" ", candidates)));
        }else {
            textBlocks.add(localization(event.getInteraction().getUserLocale(), "message.wolf.candidates.empty"));
        }

        return String.join("\n", textBlocks);
    }

    private String onlyForServerResponseText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.wolf.only_in_channel");
    }

    private String kingdomNotFoundResponseText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.wolf.kingdom_not_found");
    }

    private String memberNotFoundResponseText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.wolf.member_not_found");
    }

    private String singleManageParameterRequiredResponseText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.wolf.single_manage_parameter_required");
    }

    private String actionExpiredResponseText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.wolf.action_expired");
    }

    private WolfEditModalContext createWolfEditModalContext(Kingdom kingdom, KingdomMember member, String actionIdentity) {
        String id = "wolf.edit."+UUID.randomUUID().toString().replace("-", "");
        WolfEditModalContext context = new WolfEditModalContext(id, kingdom.getId(), member.getName(), actionIdentity);
        wolfEditContexts.put(id, context);
        return context;
    }

    private static class WolfEditModalContext {

        final String id;
        final int kingdomId;
        final String memberName;
        final String actionIdentity;

        WolfEditModalContext(String id, int kingdomId, String memberName, String actionIdentity) {
            this.id = id;
            this.kingdomId = kingdomId;
            this.memberName = memberName;
            this.actionIdentity = actionIdentity;
        }

    }

}
