package me.cepera.discord.bot.beerelemental.discord.components;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import io.netty.util.internal.ThrowableUtil;
import me.cepera.discord.bot.beerelemental.discord.DiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.DiscordToolset;
import me.cepera.discord.bot.beerelemental.local.lang.LanguageService;
import me.cepera.discord.bot.beerelemental.model.Kingdom;
import me.cepera.discord.bot.beerelemental.model.KingdomMember;
import me.cepera.discord.bot.beerelemental.repository.KingdomMemberRepository;
import me.cepera.discord.bot.beerelemental.repository.KingdomRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class KingdomDataDiscordBotComponent implements DiscordBotComponent, DiscordToolset{

    private static final Logger LOGGER = LogManager.getLogger(KingdomDataDiscordBotComponent.class);

    public static final String COMMAND_KINGDOM= "kingdom";
    public static final String COMMAND_KINGDOM_OPTION_NAME = "name";
    public static final String COMMAND_KINGDOM_OPTION_NEW_NAME = "new_name";
    public static final String COMMAND_KINGDOM_OPTION_ROLE = "role";

    public static final String COMMAND_PLAYER= "player";
    public static final String COMMAND_PLAYER_OPTION_NAME = "name";
    public static final String COMMAND_PLAYER_OPTION_NEW_NAME = "new_name";
    public static final String COMMAND_PLAYER_OPTION_KINGDOM = "kingdom";
    public static final String COMMAND_PLAYER_OPTION_USER = "user";
    public static final String COMMAND_PLAYER_OPTION_DELETE = "delete";

    private final LanguageService languageService;

    private final KingdomRepository kingdomRepository;

    private final KingdomMemberRepository memberRepository;

    @Inject
    public KingdomDataDiscordBotComponent(LanguageService languageService, KingdomRepository kingdomRepository,
            KingdomMemberRepository memberRepository) {
        this.languageService = languageService;
        this.kingdomRepository = kingdomRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public LanguageService languageService() {
        return languageService;
    }

    @Override
    public Flux<ApplicationCommandRequest> commandsToRegister() {
        return Flux.create(sink->{
            sink.next(ApplicationCommandRequest.builder()
                    .name(COMMAND_KINGDOM)
                    .nameLocalizationsOrNull(localization("command.kingdom"))
                    .description(localization(null, "command.kingdom.description"))
                    .descriptionLocalizationsOrNull(localization("command.kingdom.description"))
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_KINGDOM_OPTION_NAME)
                            .nameLocalizationsOrNull(localization("command.kingdom.option.name"))
                            .description(localization(null, "command.kingdom.option.name.description"))
                            .descriptionLocalizationsOrNull(localization("command.kingdom.option.name.description"))
                            .required(true)
                            .type(3)
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_KINGDOM_OPTION_ROLE)
                            .nameLocalizationsOrNull(localization("command.kingdom.option.role"))
                            .description(localization(null, "command.kingdom.option.role.description"))
                            .descriptionLocalizationsOrNull(localization("command.kingdom.option.role.description"))
                            .required(false)
                            .type(8)
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_KINGDOM_OPTION_NEW_NAME)
                            .nameLocalizationsOrNull(localization("command.kingdom.option.name.new"))
                            .description(localization(null, "command.kingdom.option.name.new.description"))
                            .descriptionLocalizationsOrNull(localization("command.kingdom.option.name.new.description"))
                            .required(false)
                            .type(3)
                            .build())
                    .build());

            sink.next(ApplicationCommandRequest.builder()
                    .name(COMMAND_PLAYER)
                    .nameLocalizationsOrNull(localization("command.player"))
                    .description(localization(null, "command.player.description"))
                    .descriptionLocalizationsOrNull(localization("command.player.description"))
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_PLAYER_OPTION_NAME)
                            .nameLocalizationsOrNull(localization("command.player.option.name"))
                            .description(localization(null, "command.player.option.name.description"))
                            .descriptionLocalizationsOrNull(localization("command.player.option.name.description"))
                            .required(false)
                            .type(3)
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_PLAYER_OPTION_USER)
                            .nameLocalizationsOrNull(localization("command.player.option.user"))
                            .description(localization(null, "command.player.option.user.description"))
                            .descriptionLocalizationsOrNull(localization("command.player.option.user.description"))
                            .required(false)
                            .type(6)
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_PLAYER_OPTION_KINGDOM)
                            .nameLocalizationsOrNull(localization("command.player.option.kingdom"))
                            .description(localization(null, "command.player.option.kingdom.description"))
                            .descriptionLocalizationsOrNull(localization("command.player.option.kingdom.description"))
                            .required(false)
                            .type(3)
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_PLAYER_OPTION_NEW_NAME)
                            .nameLocalizationsOrNull(localization("command.player.option.name.new"))
                            .description(localization(null, "command.player.option.name.new.description"))
                            .descriptionLocalizationsOrNull(localization("command.player.option.name.new.description"))
                            .required(false)
                            .type(3)
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_PLAYER_OPTION_DELETE)
                            .nameLocalizationsOrNull(localization("command.player.option.delete"))
                            .description(localization(null, "command.player.option.delete.description"))
                            .descriptionLocalizationsOrNull(localization("command.player.option.delete.description"))
                            .required(false)
                            .type(5)
                            .build())
                    .build());

        });
    }

    @Override
    public Mono<Void> handleChatInputInteractionEvent(ChatInputInteractionEvent event) {
        switch(getCommandName(event)) {
        case COMMAND_KINGDOM: {

            String name = event.getOption(COMMAND_KINGDOM_OPTION_NAME)
                    .flatMap(option->option.getValue().map(value->value.asString()))
                    .orElse("");

            Optional<String> maybeNewName = event.getOption(COMMAND_KINGDOM_OPTION_NEW_NAME)
                    .flatMap(option->option.getValue().map(value->value.asString()));

            Mono<Role> maybeRole = event.getOption(COMMAND_KINGDOM_OPTION_ROLE)
                    .flatMap(option->option.getValue().map(value->value.asRole()))
                    .orElse(Mono.empty());

            return handleKingdomCommand(event, name, maybeNewName, maybeRole);
        }
        case COMMAND_PLAYER: {

            Optional<String> maybeName = event.getOption(COMMAND_PLAYER_OPTION_NAME)
                    .flatMap(option->option.getValue().map(value->value.asString()));

            Optional<String> maybeNewName = event.getOption(COMMAND_PLAYER_OPTION_NEW_NAME)
                    .flatMap(option->option.getValue().map(value->value.asString()));

            Optional<String> maybeKingdom = event.getOption(COMMAND_PLAYER_OPTION_KINGDOM)
                    .flatMap(option->option.getValue().map(value->value.asString()));

            Mono<User> maybeUser = event.getOption(COMMAND_PLAYER_OPTION_USER)
                    .flatMap(option->option.getValue().map(value->value.asUser()))
                    .orElse(Mono.empty());

            boolean delete = event.getOption(COMMAND_PLAYER_OPTION_DELETE)
                    .flatMap(option->option.getValue().map(value->value.asBoolean()))
                    .orElse(false);

            return handlePlayerCommand(event, maybeName, maybeKingdom, maybeNewName, maybeUser, delete);
        }
        default:
            return Mono.empty();
        }
    }

    private Mono<Void> handleKingdomCommand(ChatInputInteractionEvent event, String name,
            Optional<String> maybeNewName, Mono<Role> maybeRole){

        String actionIdentity = createActionIdentity(event, "kingdom");

        return event.deferReply()
                .then(maybeRole)
                .map(Optional::of)
                .switchIfEmpty(Mono.fromSupplier(Optional::empty))
                .zipWith(Mono.fromSupplier(()->maybeNewName))
                .filter(tuple->tuple.getT1().isPresent() || tuple.getT2().isPresent())
                .switchIfEmpty(Mono.defer(()->sendKingdomInfo(event, name)).then(Mono.empty()))
                .flatMap(tuple->setKingdomData(event, actionIdentity, name, tuple.getT2(), tuple.getT1()))
                .onErrorResume(e->{
                    LOGGER.error("Error while executing kingdom command. Action: {} Error: {}",
                            actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return simpleEditReply(event, defaultCommandErrorText(event));
                });
    }

    private Mono<Void> sendKingdomInfo(ChatInputInteractionEvent event, String name){
        return event.getInteraction().getGuild()
                .switchIfEmpty(simpleEditReply(event, kingdomOnlyForServerResponseText(event)))
                .flatMap(guild->kingdomRepository.getKingdomByName(guild.getId().asLong(), name)
                        .switchIfEmpty(simpleEditReply(event, kingdomNotFoundResponseText(event)))
                        .zipWhen(kd->guild.getRoleById(Snowflake.of(kd.getRoleId()))
                                .map(Optional::of)
                                .switchIfEmpty(Mono.fromSupplier(Optional::empty))))
                .zipWhen(t->memberRepository.getMembers(t.getT1()).collectList(),
                        (t, members)->Tuples.of(t.getT1(), t.getT2(), members))
                .flatMap(tuple->simpleEditReply(event, kingdomDataResponseText(event, tuple.getT1(), tuple.getT2(), tuple.getT3())));
    }

    private Mono<Void> setKingdomData(ChatInputInteractionEvent event, String actionIdentity, String name,
            Optional<String> maybeNewName, Optional<Role> maybeRole){
        return event.getInteraction().getGuild()
                .switchIfEmpty(simpleEditReply(event, kingdomOnlyForServerResponseText(event)))
                .flatMap(guild->isCalledByAdmin(event.getInteraction(), guild)
                        .filter(r->r)
                        .map(r->guild)
                        .switchIfEmpty(simpleEditReply(event, kingdomOnlyForAdministratorResponseText(event))))
                .flatMap(guild->kingdomRepository.getKingdomByName(guild.getId().asLong(), name)
                        .switchIfEmpty(Mono.justOrEmpty(maybeRole)
                                .map(role->{
                                    Kingdom kd = new Kingdom();
                                    kd.setGuildId(guild.getId().asLong());
                                    kd.setName(name);
                                    kd.setRoleId(role.getId().asLong());
                                    return kd;
                                })
                                .switchIfEmpty(simpleEditReply(event, kingdomNeedRoleResponseText(event)))))
                .flatMap(kd->{
                    kd.setName(maybeNewName.orElse(kd.getName()));
                    kd.setRoleId(maybeRole.map(role->role.getId().asLong()).orElse(kd.getRoleId()));
                    return kingdomRepository.saveKingdom(kd)
                            .doOnNext(member->LOGGER.info("Clan member {} updated by action {}", member, actionIdentity));
                })
                .flatMap(kd->simpleEditReply(event, kingdomSavedResponseText(event, kd)));
    }

    private Mono<Void> handlePlayerCommand(ChatInputInteractionEvent event, Optional<String> maybeName,
            Optional<String> maybeKingdom, Optional<String> maybeNewName, Mono<User> maybeUser, boolean delete){

        String actionIdentity = createActionIdentity(event, "player");

        return event.deferReply()
                .then(event.getInteraction().getGuild())
                .switchIfEmpty(simpleEditReply(event, playerOnlyForServerResponseText(event)))
                .flatMap(guild->{
                    if(delete) {
                        return adminCheckFlatMap(event, guild)
                                .flatMap(g->deletePlayer(event, actionIdentity, guild, maybeName, maybeKingdom));
                    }else {
                        return maybeUser.map(Optional::of).switchIfEmpty(Mono.fromSupplier(Optional::empty))
                                .flatMap(optUser->{
                                    if(optUser.isPresent() && maybeName.isPresent() || maybeNewName.isPresent()
                                            || maybeName.isPresent() && maybeKingdom.isPresent()) {
                                        return adminCheckFlatMap(event, guild)
                                                .flatMap(g->setPlayerData(event, actionIdentity, guild, maybeName, maybeKingdom, maybeNewName, optUser));
                                    }else {
                                        return sendPlayerInfo(event, guild, maybeName, optUser);
                                    }
                                });
                    }
                })
                .onErrorResume(e->{
                    LOGGER.error("Error while executing player command. Action: {} Error: {}",
                            actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return simpleEditReply(event, defaultCommandErrorText(event));
                });
    }

    private Mono<Guild> adminCheckFlatMap(ApplicationCommandInteractionEvent event, Guild guild){
        return isCalledByAdmin(event.getInteraction(), guild)
                .filter(r->r)
                .map(r->guild)
        .switchIfEmpty(simpleEditReply(event, playerOnlyForAdministratorResponseText(event)));
    }

    private Mono<Void> sendPlayerInfo(ChatInputInteractionEvent event, Guild guild,
            Optional<String> maybeName, Optional<User> maybeUser){
        if(maybeUser.isPresent() && maybeName.isPresent()) {
            return simpleEditReply(event, playerOnlyNameOrUserResponseText(event));
        }
        return Mono.justOrEmpty(maybeName)
                .flatMap(name->memberRepository.findMembersByNickname(guild.getId().asLong(), name).collectList())
                .switchIfEmpty(Mono.justOrEmpty(maybeUser)
                        .switchIfEmpty(Mono.fromSupplier(event.getInteraction()::getUser).filter(user->!maybeName.isPresent()))
                        .flatMap(user->memberRepository.findMembersByUser(guild.getId().asLong(), user.getId().asLong())
                                .collectList()))
                .flatMapIterable(list->list)
                .flatMap(member->kingdomRepository.getKingdom(member.getKingdomId())
                        .switchIfEmpty(Mono.fromSupplier(()->{
                            Kingdom kd = new Kingdom();
                            kd.setName("---");
                            kd.setGuildId(guild.getId().asLong());
                            return kd;
                        }))
                        .map(kingdom->Tuples.of(kingdom, member, Optional.ofNullable(member.getDiscordUserId()))))
                .map(tuple->playerDataResponseText(event, tuple.getT1(), tuple.getT2(),
                        tuple.getT3().map(id->"<@"+id+">").orElse("---")))
                .collectList()
                .filter(list->!list.isEmpty())
                .switchIfEmpty(simpleEditReply(event, playerNotFoundResponseText(event)))
                .flatMap(infos->simpleEditReply(event, String.join("\n\n", infos)));
    }

    private Mono<Void> setPlayerData(ChatInputInteractionEvent event, String actionIdentity, Guild guild, Optional<String> maybeName,
            Optional<String> maybeKingdom, Optional<String> maybeNewName, Optional<User> maybeUser){
        return Mono.zip(Mono.justOrEmpty(maybeName), Mono.justOrEmpty(maybeKingdom))
                .switchIfEmpty(simpleEditReply(event, playerEditRequirementsResponseText(event)))
                .flatMap(tuple->kingdomRepository.getKingdomByName(guild.getId().asLong(), tuple.getT2())
                        .switchIfEmpty(simpleEditReply(event, playerEditRequirementsResponseText(event)))
                        .flatMap(kingdom->memberRepository.findMemberByNickname(kingdom, tuple.getT1())
                                .switchIfEmpty(Mono.fromSupplier(()->{
                                    KingdomMember player = new KingdomMember();
                                    player.setName(tuple.getT1());
                                    player.setKingdomId(kingdom.getId());
                                    return player;
                                }))
                                .map(member->Tuples.of(kingdom, member))))
                .map(tuple->Tuples.of(tuple.getT1(), tuple.getT2(), maybeUser))
                .flatMap(tuple->{
                   KingdomMember player = tuple.getT2();
                   player.setDiscordUserId(tuple.getT3().map(user->user.getId().asLong()).orElse(player.getDiscordUserId()));
                   player.setName(maybeNewName.orElse(player.getName()));
                   return memberRepository.saveMember(player)
                           .doOnNext(member->LOGGER.info("Clan member {} updated by action {}", member, actionIdentity))
                           .flatMap(pl->simpleEditReply(event, playerSavedResponseText(event, tuple.getT1(), pl)));
                });
    }

    private Mono<Void> deletePlayer(ChatInputInteractionEvent event, String actionIdentity, Guild guild, Optional<String> maybeName,
            Optional<String> maybeKingdom){
        return Mono.zip(Mono.justOrEmpty(maybeName), Mono.justOrEmpty(maybeKingdom))
                .switchIfEmpty(simpleEditReply(event, playerEditRequirementsResponseText(event)))
                .flatMap(tuple->kingdomRepository.getKingdomByName(guild.getId().asLong(), tuple.getT2())
                        .switchIfEmpty(simpleEditReply(event, playerKingdomNotFoundResponseText(event)))
                        .flatMap(kingdom->memberRepository.findMemberByNickname(kingdom, tuple.getT1())
                                .switchIfEmpty(simpleEditReply(event, playerNotFoundResponseText(event)))
                                .map(member->Tuples.of(kingdom, member))))
                .flatMap(tuple->memberRepository.deleteMember(tuple.getT2())
                        .doOnNext(member->LOGGER.info("Clan member {} deleted by action {}", member, actionIdentity))
                        .then(simpleEditReply(event, playerDeletedResponseText(event, tuple.getT1(), tuple.getT2()))));
    }

    private String kingdomDataResponseText(ApplicationCommandInteractionEvent event, Kingdom kingdom,
            Optional<Role> optRole, List<KingdomMember> members) {
        String kingdomName = kingdom.getName();
        String kingdomRole = optRole.map(Role::getMention).orElse("---");
        String membersDisplay = String.join("\n", IntStream.range(0, members.size())
                .mapToObj(i->{
                    KingdomMember member = members.get(i);
                    return (i+1)+". "+member.getName()+(member.getDiscordUserId() != null
                            ? " (<@"+member.getDiscordUserId()+">)" : "");
                }).collect(Collectors.toList()));
        return localization(event.getInteraction().getUserLocale(), "message.kingdom.data", "kingdom", kingdomName,
                "role", kingdomRole, "members", membersDisplay);
    }

    private String kingdomSavedResponseText(ApplicationCommandInteractionEvent event, Kingdom kingdom) {
        return localization(event.getInteraction().getUserLocale(), "message.kingdom.saved", "kingdom", kingdom.getName());
    }

    private String kingdomOnlyForAdministratorResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.kingdom.only_for_administrator");
    }

    private String kingdomOnlyForServerResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.kingdom.only_in_channel");
    }

    private String kingdomNeedRoleResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.kingdom.need_role");
    }

    private String kingdomNotFoundResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.kingdom.not_found");
    }

    private String playerDataResponseText(ApplicationCommandInteractionEvent event, Kingdom kingdom, KingdomMember member, String user) {
        return localization(event.getInteraction().getUserLocale(), "message.player.data", "kingdom", kingdom.getName(),
                "member", member.getName(), "user", user);
    }

    private String playerSavedResponseText(ApplicationCommandInteractionEvent event, Kingdom kingdom, KingdomMember member) {
        return localization(event.getInteraction().getUserLocale(), "message.player.saved", "kingdom", kingdom.getName(), "member", member.getName());
    }

    private String playerOnlyForAdministratorResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.player.only_for_administrator");
    }

    private String playerOnlyForServerResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.player.only_in_channel");
    }

    private String playerDeletedResponseText(ApplicationCommandInteractionEvent event, Kingdom kingdom, KingdomMember member) {
        return localization(event.getInteraction().getUserLocale(), "message.player.deleted", "kingdom", kingdom.getName(), "member", member.getName());
    }

    private String playerEditRequirementsResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.player.name_and_kingdom_required");
    }

    private String playerKingdomNotFoundResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.player.kingdom_not_found");
    }

    private String playerNotFoundResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.player.not_found");
    }

    private String playerOnlyNameOrUserResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.player.name_or_user_required");
    }

}
