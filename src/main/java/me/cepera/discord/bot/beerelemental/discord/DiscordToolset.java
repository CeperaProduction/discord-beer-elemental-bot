package me.cepera.discord.bot.beerelemental.discord;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Permission;
import me.cepera.discord.bot.beerelemental.local.lang.LanguageService;
import me.cepera.discord.bot.beerelemental.local.lang.Translatable;
import me.cepera.discord.bot.beerelemental.remote.RemoteService;
import me.cepera.discord.bot.beerelemental.remote.SimpleRemoteService;
import reactor.core.publisher.Mono;

public interface DiscordToolset {

    LanguageService languageService();

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

    default String getAttachmentContentUrl(Attachment attachment){
        return attachment.getProxyUrl();
    }

    default Mono<byte[]> getAttachmentContent(Attachment attachment) {
        return Mono.fromSupplier(()->attachment)
                .flatMap(att->getResource(getAttachmentContentUrl(attachment)));
    }

    default String createActionIdentity(ApplicationCommandInteractionEvent event, String prefix) {
        return prefix+"#"+UUID.randomUUID().toString().replace("-", "")+"#"+event.getInteraction().getUser().getTag();
    }

    default String defaultCommandErrorText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.command.error");
    }

    default Mono<Void> replyError(ApplicationCommandInteractionEvent event, Function<ApplicationCommandInteractionEvent, String> messageFactory, boolean edit){
        if(edit) {
            return event.editReply(messageFactory.apply(event)).then();
        }else {
            return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                    .content(messageFactory.apply(event))
                    .ephemeral(true)
                    .build());
        }
    }

    default Mono<Boolean> isCalledByAdmin(Interaction interaction) {
        return isCalledByAdmin(interaction, Member::getGuild);
    }

    default Mono<Boolean> isCalledByAdmin(Interaction interaction, Guild guild) {
        return isCalledByAdmin(interaction, member->Mono.just(guild));
    }

    default Mono<Boolean> isCalledByAdmin(Interaction interaction, Function<Member, Mono<Guild>> guildReceiverGetter) {
        return Mono.justOrEmpty(interaction.getMember())
                .flatMap(member->member.getHighestRole()
                                .filter(role->role.getPermissions().contains(Permission.ADMINISTRATOR))
                                .map(r->true)
                                .switchIfEmpty(guildReceiverGetter.apply(member)
                                        .filter(g->g.getOwnerId().equals(member.getId()))
                                        .map(g->true)))
                .switchIfEmpty(Mono.just(false));
    }

    default <T> Mono<T> simpleReply(ApplicationCommandInteractionEvent event, String content) {
        return simpleReply(event, content, true);
    }

    default <T> Mono<T> simpleReply(ApplicationCommandInteractionEvent event, String content, boolean ephemeral){
        return Mono.defer(()->event.reply()
                .withContent(content)
                .withEphemeral(ephemeral)
                .withAllowedMentions(AllowedMentions.suppressAll())
                .then(Mono.empty()));
    }

    default <T> Mono<T> simpleEditReply(ApplicationCommandInteractionEvent event, String content){
        return Mono.defer(()->event.editReply()
                .withContentOrNull(content)
                .withAllowedMentionsOrNull(AllowedMentions.suppressAll())
                .then(Mono.empty()));
    }



}
