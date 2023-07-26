package me.cepera.discord.bot.beerelemental.discord.modules;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.InteractionCallbackSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.entity.RestGuild;
import discord4j.rest.entity.RestMessage;
import io.netty.util.internal.ThrowableUtil;
import me.cepera.discord.bot.beerelemental.discord.DiscordBot;
import me.cepera.discord.bot.beerelemental.discord.DiscordBotModule;
import me.cepera.discord.bot.beerelemental.discord.DiscordToolset;
import me.cepera.discord.bot.beerelemental.local.lang.LanguageLocalService;
import me.cepera.discord.bot.beerelemental.model.ActiveAuction;
import me.cepera.discord.bot.beerelemental.repository.ActiveAuctionRepository;
import me.cepera.discord.bot.beerelemental.repository.GuildLocaleRepository;
import me.cepera.discord.bot.beerelemental.utils.ImageUtils;
import me.cepera.discord.bot.beerelemental.utils.TimeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class AuctionDiscordBotModule implements DiscordBotModule, DiscordToolset {

    private static final Logger LOGGER = LogManager.getLogger(AuctionDiscordBotModule.class);

    public static final String COMMAND_AUCTION= "auction";
    public static final String COMMAND_OPTION_ITEM = "item";
    public static final String COMMAND_OPTION_COUNT = "count";
    public static final String COMMAND_OPTION_ROLE = "role";
    public static final String COMMAND_OPTION_TIME = "time";

    private final LanguageLocalService languageService;

    private final ActiveAuctionRepository activeAuctionRepository;

    private final GuildLocaleRepository guildLocaleRepository;

    @Inject
    public AuctionDiscordBotModule(LanguageLocalService languageService,
            ActiveAuctionRepository activeAuctionRepository, GuildLocaleRepository guildLocaleRepository) {
        this.languageService = languageService;
        this.activeAuctionRepository = activeAuctionRepository;
        this.guildLocaleRepository = guildLocaleRepository;
    }

    @Override
    public LanguageLocalService languageService() {
        return languageService;
    }

    @Override
    public Flux<ApplicationCommandRequest> commandsToRegister() {
        return Flux.create(sink->{
            sink.next(ApplicationCommandRequest.builder()
                    .name(COMMAND_AUCTION)
                    .nameLocalizationsOrNull(localization("command.auction"))
                    .description(localization(null, "command.auction.description"))
                    .descriptionLocalizationsOrNull(localization("command.auction.description"))
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_ITEM)
                            .nameLocalizationsOrNull(localization("command.auction.option.item"))
                            .description(localization(null, "command.auction.option.item.description"))
                            .descriptionLocalizationsOrNull(localization("command.auction.option.item.description"))
                            .type(11)
                            .required(true)
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_COUNT)
                            .nameLocalizationsOrNull(localization("command.auction.option.count"))
                            .description(localization(null, "command.auction.option.count.description"))
                            .descriptionLocalizationsOrNull(localization("command.auction.option.count.description"))
                            .required(true)
                            .minValue(1.0D)
                            .maxValue(100.0D)
                            .type(4)
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_ROLE)
                            .nameLocalizationsOrNull(localization("command.auction.option.role"))
                            .description(localization(null, "command.auction.option.role.description"))
                            .descriptionLocalizationsOrNull(localization("command.auction.option.role.description"))
                            .required(true)
                            .type(8)
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_TIME)
                            .nameLocalizationsOrNull(localization("command.auction.option.time"))
                            .description(localization(null, "command.auction.option.time.description"))
                            .descriptionLocalizationsOrNull(localization("command.auction.option.time.description"))
                            .required(false)
                            .type(3)
                            .build())
                    .build());
        });
    }

    @Override
    public Mono<Void> handleChatInputInteractionEvent(ChatInputInteractionEvent event) {
        if(!getCommandName(event).equals(COMMAND_AUCTION)) {
            return Mono.empty();
        }

        Attachment attachment = event.getOption(COMMAND_OPTION_ITEM)
                .flatMap(option->option.getValue())
                .map(value->value.asAttachment())
                .orElse(null);

        int count = event.getOption(COMMAND_OPTION_COUNT)
                .flatMap(option->option.getValue())
                .map(value->(int)value.asLong())
                .orElse(1);

        Mono<Role> maybeRole = event.getOption(COMMAND_OPTION_ROLE)
                .flatMap(option->option.getValue())
                .map(value->value.asRole())
                .orElseGet(()->Mono.empty());

        String timeString = event.getOption(COMMAND_OPTION_TIME)
                .flatMap(option->option.getValue())
                .map(value->value.asString().trim())
                .orElse("4h");

        return handleAuctionCommand(event, attachment, count, maybeRole, timeString);

    }

    private Mono<Void> handleAuctionCommand(ChatInputInteractionEvent event, Attachment attachment, int count,
            Mono<Role> maybeRole, String timeOffsetString){

        String actionIdentity = createActionIdentity(event, "auction");

        if(attachment != null) {
            String contentType = attachment.getContentType().orElse("");
            LOGGER.info("Received attachment with type {} for action {}", contentType, actionIdentity);
            if(!isImage(contentType)) {
                return replyError(event, this::wrongAttachmentResponseText, true);
            }
        }

        return event.deferReply(InteractionCallbackSpec.builder()
                    .ephemeral(false)
                    .build())
                .then(event.getInteraction().getGuild())
                .switchIfEmpty(Mono.defer(()->
                    replyError(event, this::onlyForChannelResponseText, true).then(Mono.empty())))
                .zipWith(event.getInteraction().getChannel().switchIfEmpty(Mono.defer(()->
                    replyError(event, this::onlyForChannelResponseText, true).then(Mono.empty()))))
                .zipWith(maybeRole.switchIfEmpty(Mono.defer(()->
                    replyError(event, this::wrongTargetRoleResponseText, true).then(Mono.empty()))),
                        (tuple, role)->Tuples.of(tuple.getT1(), tuple.getT2(), role))
                .zipWith(TimeUtils.getInstant(timeOffsetString).onErrorResume(e->replyError(event, this::wrongTimeResponseText, true).then(Mono.empty())),
                        (tuple, instant)->Tuples.of(tuple.getT1(), tuple.getT2(), tuple.getT3(), instant))
                .zipWith(getAttachmentContent(attachment)
                        .filter(bytes->bytes.length > 0)
                        .switchIfEmpty(Mono.defer(()->replyError(event, this::wrongAttachmentResponseText, true).then(Mono.empty()))),
                        (tuple, item)->Tuples.of(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), item))
                .flatMap(tuple->{
                    Guild guild = tuple.getT1();
                    MessageChannel channel = tuple.getT2();
                    byte[] itemBytes = tuple.getT5();
                    Role role = tuple.getT3();
                    Instant rollTime = tuple.getT4();
                    return sendAuctionStartMessage(event, guild, itemBytes, count, role, rollTime)
                            .flatMap(message->{
                                ActiveAuction auction = createAuction(guild, channel, count, role, message, rollTime);
                                return activeAuctionRepository.saveAuction(auction)
                                    .then(Mono.fromRunnable(()->LOGGER.info("Auction {} created. Action: {}", auction, actionIdentity)).then());
                            });
                })
                .onErrorResume(e->{
                    LOGGER.error("Error during auction creation. Action: {} Error: {}", actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return replyError(event, this::defaultCommandErrorText, true);
                });

    }

    private ActiveAuction createAuction(Guild guild, MessageChannel channel, int count, Role role, Message message, Instant rollTime) {
        ActiveAuction activeAuction = new ActiveAuction();
        activeAuction.setCount(count);
        activeAuction.setGuildId(guild.getId().asLong());
        activeAuction.setChannelId(channel.getId().asLong());
        activeAuction.setRoleId(role.isEveryone() ? 0 : role.getId().asLong());
        activeAuction.setMessageId(message.getId().asLong());
        activeAuction.setTimestamp(rollTime.toEpochMilli());
        return activeAuction;
    }

    private Mono<Message> sendAuctionStartMessage(ChatInputInteractionEvent event, Guild guild, byte[] itemBytes, int count, Role role, Instant rollTime) {
        MessageCreateFields.File file = MessageCreateFields.File.of("item.png",
                new ByteArrayInputStream(ImageUtils.writeImagePng(ImageUtils.readImage(itemBytes))));
        return guildLocaleRepository.getGuildLocale(guild.getId().asLong())
                .flatMap(guildLocale->event.editReply()
                        .withContentOrNull(auctionPrepareText(guildLocale.getLanguageTag()))
                        .then(event.getReply())
                        .flatMap(msg->event.getInteraction().getChannel()
                                .flatMap(channel->channel.createMessage(MessageCreateSpec.builder()
                                        .content(auctionStartResponseText(guildLocale.getLanguageTag(), count, role, rollTime))
                                        .addFile(file)
                                        .messageReference(msg.getId())
                                        .build()))));
    }

    public Mono<Void> completeEndedAuctions(DiscordBot bot){
        return activeAuctionRepository.getEndedActiveAuctions()
                .flatMap(auction->completeAuction(bot, auction))
                .then();
    }

    private Mono<Void> completeAuction(DiscordBot bot, ActiveAuction auction){

        RestGuild guild = bot.getDiscordClient().getGuildById(Snowflake.of(auction.getGuildId()));

        RestChannel channel = bot.getDiscordClient().getChannelById(Snowflake.of(auction.getChannelId()));

        RestMessage message = channel.getRestMessage(Snowflake.of(auction.getMessageId()));

        Random rand = new Random();

        return message.getData()
            .flatMap(messageData->Mono.justOrEmpty(messageData.reactions().toOptional()))
            .flatMapIterable(list->list)
            .flatMap(reaction->Mono.justOrEmpty(reaction.emoji().name()))
            .flatMap(reaction->bot.getDiscordClient().getChannelService()
                    .getReactions(auction.getChannelId(), auction.getMessageId(), reaction, Collections.emptyMap()))
            .map(userData->userData.id().asLong())
            .collect(Collectors.toSet())
            .flatMapIterable(ids->ids)
            .flatMap(userId->guild.getMember(Snowflake.of(userId)))
            .filter(member->auction.getRoleId() == 0 ? true : member.roles().stream().map(Id::asLong).anyMatch(roleId->auction.getRoleId() == roleId))
            .map(memberData->"<@"+memberData.user().id().asLong()+">")
            .collectList()
            .filter(list->!list.isEmpty())
            .map(members->{
                List<String> possible = new ArrayList<>(members);
                Set<String> winnedSet = new HashSet<>();
                while(!possible.isEmpty() && winnedSet.size() < auction.getCount()) {
                    winnedSet.add(possible.remove(rand.nextInt(possible.size())));
                }
                List<String> participants = new ArrayList<String>();
                for(int i = 0; i < members.size(); ++i) {
                    String member = members.get(i);
                    if(winnedSet.contains(member)) {
                        participants.add("**"+i+". "+member+"**");
                    }else {
                        participants.add(i+". "+member);
                    }
                }
                return Tuples.of(participants, new ArrayList<>(winnedSet));
            })
            .zipWith(guild.getData(), (tuple, g)->Tuples.of(tuple.getT1(), tuple.getT2(), g))
            .zipWhen(tuple->guildLocaleRepository.getGuildLocale(tuple.getT3().id().asLong()),
                    (tuple, locale)->Tuples.of(tuple.getT1(), tuple.getT2(), tuple.getT3(), locale))
            .flatMap(tuple->channel.createMessage(MessageCreateRequest.builder()
                            .messageReference(MessageReferenceData.builder()
                                    .guildId(tuple.getT3().id())
                                    .channelId(channel.getId().asLong())
                                    .messageId(message.getId().asLong())
                                    .build())
                        .content(auctionParticipantsText(tuple.getT4().getLanguageTag(), tuple.getT1()))
                        .allowedMentions(AllowedMentionsData.builder()
                                .users(Collections.emptyList())
                                .build())
                        .build())
                    .flatMap(participantsMessage->channel.createMessage(MessageCreateRequest.builder()
                            .messageReference(MessageReferenceData.builder()
                                    .guildId(tuple.getT3().id())
                                    .channelId(participantsMessage.channelId())
                                    .messageId(participantsMessage.id())
                                    .build())
                        .content(auctionResultText(tuple.getT4().getLanguageTag(), tuple.getT2()))
                        .build())))
            .switchIfEmpty(Mono.fromRunnable(()->LOGGER.info("Can't find any winner for auction {}", auction))
                    .then(guild.getData())
                    .zipWhen(guildData->Mono.justOrEmpty(guildData.roles().stream()
                            .filter(rd->auction.getRoleId() == 0 ? false : rd.id().asLong() == auction.getRoleId())
                            .map(rd->"<@&"+rd.id().asLong()+">")
                            .findAny())
                            .switchIfEmpty(Mono.just("")))
                    .zipWhen(tuple->guildLocaleRepository.getGuildLocale(tuple.getT1().id().asLong()),
                            (tuple, locale)->Tuples.of(tuple.getT1(), tuple.getT2(), locale))
                    .flatMap(tuple->channel.createMessage(MessageCreateRequest.builder()
                            .content(auctionResultNoParticipantsText(tuple.getT3().getLanguageTag(), tuple.getT2()))
                            .messageReference(MessageReferenceData.builder()
                                    .guildId(tuple.getT1().id())
                                    .channelId(channel.getId().asLong())
                                    .messageId(message.getId().asLong())
                                    .build())
                            .build())))
            .onErrorResume(e->Mono.fromRunnable(()->
                LOGGER.error("Error while auction completing auction {} Error {}", auction, ThrowableUtil.stackTraceToString(e))))
            .then(activeAuctionRepository.deleteAuction(auction).then(Mono.fromRunnable(()->
                LOGGER.info("Auction {} completed.", auction)).then()));
    }



    private boolean isImage(String contentType) {
        return contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg");
    }

    private String auctionPrepareText(String locale) {
        return localization(locale, "message.auction.prepare");
    }

    private String auctionStartResponseText(String locale, int count, Role role, Instant rollingTime) {
        if(count > 1) {
            return localization(locale, "message.auction.start.many", "role",
                    role.getMention(), "count", Integer.toString(count), "time", String.format("<t:%d:t>", rollingTime.getEpochSecond()));
        }else {
            return localization(locale, "message.auction.start", "role",
                    role.getMention(), "time", String.format("<t:%d:t>", rollingTime.getEpochSecond()));
        }
    }

    private String auctionParticipantsText(String locale, List<String> participants) {
        return localization(locale, "message.auction.participants", "participants", String.join("\n", participants));
    }

    private String auctionResultText(String locale, List<String> winners) {
        return localization(locale, "message.auction.results", "winners", String.join(", ", winners));
    }

    private String auctionResultNoParticipantsText(String locale, String roleMention) {
        return localization(locale, "message.auction.no_participants", "role", roleMention);
    }

    private String wrongAttachmentResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.auction.wrong_attachment");
    }

    private String wrongTargetRoleResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.auction.wrong_role");
    }

    private String wrongTimeResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.auction.wrong_time");
    }

    private String onlyForChannelResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.auction.only_in_channel");
    }

}
