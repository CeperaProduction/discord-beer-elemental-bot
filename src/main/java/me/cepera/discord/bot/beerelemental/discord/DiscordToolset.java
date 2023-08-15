package me.cepera.discord.bot.beerelemental.discord;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption.Type;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.AllowedMentions;
import me.cepera.discord.bot.beerelemental.local.PermissionService;
import me.cepera.discord.bot.beerelemental.local.lang.LanguageService;
import me.cepera.discord.bot.beerelemental.local.lang.Translatable;
import me.cepera.discord.bot.beerelemental.model.Permission;
import me.cepera.discord.bot.beerelemental.remote.RemoteService;
import me.cepera.discord.bot.beerelemental.remote.SimpleRemoteService;
import reactor.core.publisher.Mono;

public interface DiscordToolset {

    LanguageService languageService();

    PermissionService permissionService();

    RemoteService defaultHttpService = new SimpleRemoteService();

    default RemoteService httpService() {
        return defaultHttpService;
    }

    default String getCommandName(ApplicationCommandInteractionEvent event) {
        String command = event.getCommandName();
        if(command.startsWith("/")) {
            command = command.substring(1);
        }
        return command.toLowerCase();
    }

    default String getCommandName(ChatInputAutoCompleteEvent event) {
        String command = event.getCommandName();
        if(command.startsWith("/")) {
            command = command.substring(1);
        }
        return command.toLowerCase();
    }

    default Optional<ApplicationCommandInteractionOption> getSubCommand(ChatInputInteractionEvent event){
        return getSubCommand(event.getOptions());
    }

    default Optional<ApplicationCommandInteractionOption> getSubCommand(ChatInputAutoCompleteEvent event){
        return getSubCommand(event.getOptions());
    }

    default Optional<ApplicationCommandInteractionOption> getSubCommand(List<ApplicationCommandInteractionOption> options) {
        return options.stream().filter(opt->opt.getType() == Type.SUB_COMMAND).findAny();
    }

    @Nullable
    default Map<String, String> localization(String key){
        return languageService().getLocalizations(key);
    }

    @Nullable
    default Map<String, String> localization(Translatable translatable){
        return localization(translatable.getLangKey());
    }

    default String localization(String locale, Translatable translatable){
        return localization(locale, translatable.getLangKey());
    }

    default String localization(String locale, String key, String... replacementPairs) {
        Map<String, String> langToValue = languageService().getLocalizations(key, replacementPairs);
        if(langToValue == null) {
            return key;
        }
        String l = Optional.ofNullable(locale).filter(s->!s.isEmpty()).orElse("en-US");
        return langToValue.getOrDefault(l, langToValue.getOrDefault("en-US", key));
    }

    default Mono<byte[]> getResource(String url){
        return httpService().get(URI.create(url));
    }

    default String getAttachmentContentUrl(Attachment attachment) {
        return getAttachmentContentUrl(attachment, true);
    }

    default String getAttachmentContentUrl(Attachment attachment, boolean proxy){
        return proxy ? attachment.getProxyUrl() : attachment.getUrl();
    }

    default Mono<byte[]> getAttachmentContent(Attachment attachment){
        return getAttachmentContent(attachment, true);
    }

    default Mono<byte[]> getAttachmentContent(Attachment attachment, boolean proxy) {
        return Mono.fromSupplier(()->attachment)
                .flatMap(att->getResource(getAttachmentContentUrl(attachment, proxy)));
    }

    default String createActionIdentity(DeferrableInteractionEvent event, String prefix) {
        return prefix+"#"+UUID.randomUUID().toString().replace("-", "")+"#"+event.getInteraction().getUser().getTag();
    }

    default String defaultCommandErrorText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.command.error");
    }

    default String defaultNoPermissionText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.command.no_permission");
    }

    default String defaultOnlyForAdminText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.command.only_for_administrator");
    }

    default Mono<Void> replyError(DeferrableInteractionEvent event, Function<DeferrableInteractionEvent, String> messageFactory, boolean edit){
        if(edit) {
            return event.editReply(messageFactory.apply(event)).then();
        }else {
            return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                    .content(messageFactory.apply(event))
                    .ephemeral(true)
                    .build());
        }
    }

    default Mono<Boolean> hasPermission(Guild guild, Member member, Permission permission){
        return permissionService().hasPermission(guild, member, permission);
    }

    default Mono<Boolean> hasPermission(Interaction interaction, Guild guild, Permission permission){
        return Mono.justOrEmpty(interaction.getMember())
                .flatMap(member->permissionService().hasPermission(guild, member, permission));
    }

    default Mono<Boolean> hasPermission(Interaction interaction, Permission permission){
        return Mono.zip(Mono.justOrEmpty(interaction.getMember()), interaction.getGuild())
                .flatMap(tuple->permissionService().hasPermission(tuple.getT2(), tuple.getT1(), permission));
    }

    default <T> Mono<T> handlePermissionCheck(T chainItem, Mono<Boolean> permissionCheck, Mono<?> replyHandler){
        return permissionCheck
                .filter(r->r)
                .map(r->chainItem)
                .switchIfEmpty(Mono.defer(()->replyHandler).then(Mono.empty()));
    }

    default <T> Mono<T> simpleReply(DeferrableInteractionEvent event, String content) {
        return simpleReply(event, content, true);
    }

    default <T> Mono<T> simpleReply(DeferrableInteractionEvent event, String content, boolean ephemeral){
        return Mono.defer(()->event.reply()
                .withContent(content)
                .withEphemeral(ephemeral)
                .withAllowedMentions(AllowedMentions.suppressAll())
                .then(Mono.empty()));
    }

    default <T> Mono<T> simpleEditReply(DeferrableInteractionEvent event, String content){
        return Mono.defer(()->event.editReply()
                .withContentOrNull(content)
                .withAllowedMentionsOrNull(AllowedMentions.suppressAll())
                .then(Mono.empty()));
    }



}
