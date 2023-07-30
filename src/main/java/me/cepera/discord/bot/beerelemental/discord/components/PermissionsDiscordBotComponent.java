package me.cepera.discord.bot.beerelemental.discord.components;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import io.netty.util.internal.ThrowableUtil;
import me.cepera.discord.bot.beerelemental.discord.DiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.DiscordToolset;
import me.cepera.discord.bot.beerelemental.local.PermissionService;
import me.cepera.discord.bot.beerelemental.local.lang.LanguageService;
import me.cepera.discord.bot.beerelemental.model.Permission;
import me.cepera.discord.bot.beerelemental.model.RolePermission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PermissionsDiscordBotComponent implements DiscordBotComponent, DiscordToolset {

    private static final Logger LOGGER = LogManager.getLogger(PermissionsDiscordBotComponent.class);

    public static final String COMMAND_PERMISSION= "permission";
    public static final String COMMAND_OPTION_PERMISSION = "permission";
    public static final String COMMAND_OPTION_ROLE = "role";
    public static final String COMMAND_OPTION_VALUE = "value";

    private final LanguageService languageService;

    private final PermissionService permissionService;

    @Inject
    public PermissionsDiscordBotComponent(LanguageService languageService, PermissionService permissionService) {
        this.languageService = languageService;
        this.permissionService = permissionService;
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
                    .name(COMMAND_PERMISSION)
                    .nameLocalizationsOrNull(localization("command.permission"))
                    .description(localization(null, "command.permission.description"))
                    .descriptionLocalizationsOrNull(localization("command.permission.description"))
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_PERMISSION)
                            .nameLocalizationsOrNull(localization("command.permission.option.permission"))
                            .description(localization(null, "command.permission.option.permission.description"))
                            .descriptionLocalizationsOrNull(localization("command.permission.option.permission.description"))
                            .type(3)
                            .addAllChoices(Arrays.stream(Permission.values())
                                    .map(permission->ApplicationCommandOptionChoiceData.builder()
                                            .name(permission.getValue())
                                            .nameLocalizationsOrNull(localization(permissionLangKey(permission)))
                                            .value(permission.getValue())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_ROLE)
                            .nameLocalizationsOrNull(localization("command.permission.option.role"))
                            .description(localization(null, "command.permission.option.role.description"))
                            .descriptionLocalizationsOrNull(localization("command.permission.option.role.description"))
                            .type(8)
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_VALUE)
                            .nameLocalizationsOrNull(localization("command.permission.option.value"))
                            .description(localization(null, "command.permission.option.value.description"))
                            .descriptionLocalizationsOrNull(localization("command.permission.option.value.description"))
                            .type(5)
                            .build())
                    .build());
        });
    }

    @Override
    public Mono<Void> handleChatInputInteractionEvent(ChatInputInteractionEvent event) {
        if(!getCommandName(event).equals(COMMAND_PERMISSION)) {
            return Mono.empty();
        }

        Optional<Permission> optPerm = event.getOption(COMMAND_OPTION_PERMISSION)
                .flatMap(opt->opt.getValue())
                .flatMap(val->Optional.ofNullable(Permission.fromValue(val.asString())));

        Optional<Mono<Role>> optMaybeRole = event.getOption(COMMAND_OPTION_ROLE)
                .flatMap(opt->opt.getValue())
                .map(val->val.asRole());

        Optional<Boolean> optValue = event.getOption(COMMAND_OPTION_VALUE)
                .flatMap(opt->opt.getValue())
                .map(val->val.asBoolean());

        return handlePermissionCommand(event, optMaybeRole, optPerm, optValue);
    }

    private Mono<Void> handlePermissionCommand(ChatInputInteractionEvent event, Optional<Mono<Role>> optMaybeRole,
            Optional<Permission> optPermission, Optional<Boolean> optValue){

        String actionIdentity = createActionIdentity(event, "permission");

        return event.deferReply()
                .withEphemeral(false)
                .then(event.getInteraction().getGuild())
                .switchIfEmpty(simpleEditReply(event, onlyForServerResponseText(event)).then(Mono.empty()))
                .flatMap(guild->handlePermissionCheck(guild,
                            Mono.justOrEmpty(event.getInteraction().getMember())
                                .flatMap(member->permissionService().isAdmin(guild, member)),
                            simpleEditReply(event, defaultOnlyForAdminText(event))))
                .flatMap(guild->{
                    if(!optPermission.isPresent() && !optMaybeRole.isPresent() && !optValue.isPresent()) {
                        return sendPermissions(event, guild);
                    }else {
                        return setPermission(event, actionIdentity, guild, optMaybeRole, optPermission, optValue);
                    }
                })
                .onErrorResume(e->{
                    LOGGER.error("Error while processing permission command. Action: {} Error: {}",
                            actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return simpleEditReply(event, defaultCommandErrorText(event));
                });

    }

    private Mono<Void> sendPermissions(ChatInputInteractionEvent event, Guild guild){
        return permissionService.getRolePermissions(guild.getId().asLong())
                .collectMultimap(RolePermission::getPermission)
                .flatMap(permissionsMap->Flux.fromArray(Permission.values())
                        .map(perm->{
                            List<String> holders = permissionsMap.getOrDefault(perm, Collections.emptyList()).stream()
                                    .map(RolePermission::getRoleId)
                                    .map(roleId->"<@&"+roleId+">")
                                    .collect(Collectors.toList());
                            if(holders.isEmpty()) {
                                return "**"+permissionText(event, perm)+"**\n> "+adminOnlyAccessText(event);
                            }else {
                                return "**"+permissionText(event, perm)+"**\n> "+String.join(", ", holders);
                            }
                        })
                        .collectList())
                .flatMap(permissionInfos->simpleEditReply(event, String.join("\n\n", permissionInfos)));
    }

    private Mono<Void> setPermission(ChatInputInteractionEvent event, String actionIdentity, Guild guild, Optional<Mono<Role>> optMaybeRole,
            Optional<Permission> optPermission, Optional<Boolean> optValue){
        if(!optMaybeRole.isPresent() || !optPermission.isPresent() || !optValue.isPresent()) {
            return simpleEditReply(event, allArgumentsRequiredResponseText(event));
        }
        Mono<Role> maybeRole = optMaybeRole.get();
        Permission permission = optPermission.get();
        boolean value = optValue.get();
        return maybeRole
                .switchIfEmpty(simpleEditReply(event, roleNotFoundResponseText(event)).then(Mono.empty()))
                .flatMap(role->permissionService.setRolePermission(guild.getId().asLong(), role.getId().asLong(), permission, value)
                        .then(Mono.fromRunnable(()->LOGGER.info("Access to {} in guild {} for role {} was changed to {} by action {}",
                                permission, guild.getId().asLong(), role.getId().asLong(), value, actionIdentity)))
                        .then(simpleEditReply(event, permissionSetResponseText(event, permission, role, value))));
    }

    private String permissionSetResponseText(ApplicationCommandInteractionEvent event, Permission perm,
            Role role, boolean value) {
        return localization(event.getInteraction().getUserLocale(), "message.permission.set."+value,
                "permission", permissionText(event, perm),
                "role", role.getMention());
    }

    private String permissionText(ApplicationCommandInteractionEvent event, Permission perm) {
        return localization(event.getInteraction().getUserLocale(), permissionLangKey(perm));
    }

    private String onlyForServerResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.permission.only_in_channel");
    }

    private String adminOnlyAccessText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.permission.admin_only");
    }

    private String roleNotFoundResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.permission.role_not_found");
    }

    private String allArgumentsRequiredResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.permission.all_arguments_required");
    }

    private String permissionLangKey(Permission permission) {
        return "permission."+permission.getValue();
    }

}
