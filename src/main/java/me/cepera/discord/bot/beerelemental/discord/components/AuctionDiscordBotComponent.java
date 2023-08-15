package me.cepera.discord.bot.beerelemental.discord.components;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
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
import discord4j.rest.util.AllowedMentions;
import io.netty.util.internal.ThrowableUtil;
import me.cepera.discord.bot.beerelemental.discord.DiscordBot;
import me.cepera.discord.bot.beerelemental.discord.DiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.DiscordToolset;
import me.cepera.discord.bot.beerelemental.local.PermissionService;
import me.cepera.discord.bot.beerelemental.local.RandomService;
import me.cepera.discord.bot.beerelemental.local.lang.LanguageService;
import me.cepera.discord.bot.beerelemental.model.ActiveAuction;
import me.cepera.discord.bot.beerelemental.model.GuildLocale;
import me.cepera.discord.bot.beerelemental.model.KingdomMember;
import me.cepera.discord.bot.beerelemental.model.Permission;
import me.cepera.discord.bot.beerelemental.repository.ActiveAuctionRepository;
import me.cepera.discord.bot.beerelemental.repository.GuildLocaleRepository;
import me.cepera.discord.bot.beerelemental.repository.KingdomMemberRepository;
import me.cepera.discord.bot.beerelemental.repository.KingdomRepository;
import me.cepera.discord.bot.beerelemental.utils.ImageFormat;
import me.cepera.discord.bot.beerelemental.utils.ImageUtils;
import me.cepera.discord.bot.beerelemental.utils.TimeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class AuctionDiscordBotComponent implements DiscordBotComponent, DiscordToolset {

    private static final Logger LOGGER = LogManager.getLogger(AuctionDiscordBotComponent.class);

    public static final String COMMAND_AUCTION= "auction";
    public static final String COMMAND_OPTION_ITEM = "item";
    public static final String COMMAND_OPTION_COUNT = "count";
    public static final String COMMAND_OPTION_ROLE = "role";
    public static final String COMMAND_OPTION_TIME = "time";
    public static final String COMMAND_OPTION_MEMBERS = "members";

    private final LanguageService languageService;

    private final PermissionService permissionService;

    private final ActiveAuctionRepository activeAuctionRepository;

    private final GuildLocaleRepository guildLocaleRepository;

    private final KingdomRepository kingdomRepository;

    private final KingdomMemberRepository kingdomMemberRepository;

    private final RandomService randomService;

    @Inject
    public AuctionDiscordBotComponent(LanguageService languageService, PermissionService permissionService,
            ActiveAuctionRepository activeAuctionRepository, GuildLocaleRepository guildLocaleRepository,
            KingdomRepository kingdomRepository, KingdomMemberRepository kingdomMemberRepository,
            RandomService randomService) {
        this.languageService = languageService;
        this.permissionService = permissionService;
        this.activeAuctionRepository = activeAuctionRepository;
        this.guildLocaleRepository = guildLocaleRepository;
        this.kingdomRepository = kingdomRepository;
        this.kingdomMemberRepository = kingdomMemberRepository;
        this.randomService = randomService;
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
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_MEMBERS)
                            .nameLocalizationsOrNull(localization("command.auction.option.members"))
                            .description(localization(null, "command.auction.option.members.description"))
                            .descriptionLocalizationsOrNull(localization("command.auction.option.members.description"))
                            .required(false)
                            .type(3)
                            .build())
                    .build());
        });
    }

    @Override
    public Mono<Void> handleChatInputInteractionEvent(ChatInputInteractionEvent event, DiscordBot bot) {
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

        Optional<String> timeString = event.getOption(COMMAND_OPTION_TIME)
                .flatMap(option->option.getValue())
                .map(value->value.asString().trim());

        Optional<String> members = event.getOption(COMMAND_OPTION_MEMBERS)
                .flatMap(option->option.getValue())
                .map(value->value.asString().trim());

        return handleAuctionCommand(event, attachment, count, maybeRole, timeString, members);

    }

    private Mono<Void> handleAuctionCommand(ChatInputInteractionEvent event, Attachment attachment, int count,
            Mono<Role> maybeRole, Optional<String> timeString, Optional<String> members){

        String actionIdentity = createActionIdentity(event, "auction");

        if(attachment != null) {
            String contentType = attachment.getContentType().orElse("");
            LOGGER.info("Received attachment with type {} for action {}", contentType, actionIdentity);
            if(!isImage(contentType)) {
                return replyError(event, this::wrongAttachmentResponseText, false);
            }
        }

        return event.deferReply()
                .withEphemeral(false)
                .then(event.getInteraction().getGuild())
                .switchIfEmpty(Mono.defer(()->
                    replyError(event, this::onlyForChannelResponseText, true).then(Mono.empty())))
                .flatMap(guild->handlePermissionCheck(guild,
                        hasPermission(event.getInteraction(), guild, Permission.START_AUCTION),
                        simpleEditReply(event, defaultNoPermissionText(event))))
                .zipWith(event.getInteraction().getChannel().switchIfEmpty(Mono.defer(()->
                    replyError(event, this::onlyForChannelResponseText, true).then(Mono.empty()))))
                .zipWith(maybeRole.switchIfEmpty(Mono.defer(()->
                    replyError(event, this::wrongTargetRoleResponseText, true).then(Mono.empty()))),
                        (tuple, role)->Tuples.of(tuple.getT1(), tuple.getT2(), role))
                .zipWith(getAttachmentContent(attachment)
                        .filter(bytes->bytes.length > 0)
                        .switchIfEmpty(Mono.defer(()->replyError(event, this::wrongAttachmentResponseText, true).then(Mono.empty()))),
                        (tuple, item)->Tuples.of(tuple.getT1(), tuple.getT2(), tuple.getT3(), item))
                .flatMap(tuple->{
                    if(timeString.isPresent() && members.isPresent()) {
                        return replyError(event, this::onlyTimeOrMembersResponseText, true);
                    }
                    if(members.isPresent()) {
                        List<String> memberList = Arrays.stream(members.get().split("\\s"))
                                .map(String::trim).filter(str->!str.isEmpty()).collect(Collectors.toList());
                        memberList = new ArrayList<>(new LinkedHashSet<>(memberList));
                        return doInstantAuction(event, actionIdentity, tuple.getT4(), count, tuple.getT1(), tuple.getT2(), tuple.getT3(), memberList);
                    }
                    return startActiveAuction(event, actionIdentity, tuple.getT4(), count, tuple.getT1(), tuple.getT2(), tuple.getT3(), timeString.orElse("4h"));
                })
                .onErrorResume(e->{
                    LOGGER.error("Error during auction creation. Action: {} Error: {}", actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return replyError(event, this::defaultCommandErrorText, true);
                });

    }

    private Mono<Void> startActiveAuction(ChatInputInteractionEvent event, String actionIdentity, byte[] itemBytes, int count,
            Guild guild, MessageChannel channel, Role role, String timeOffsetString){
        return TimeUtils.getInstant(timeOffsetString).onErrorResume(e->replyError(event, this::wrongTimeResponseText, true).then(Mono.empty()))
                .flatMap(rollTime->sendAuctionStartMessage(event, guild, itemBytes, count, role, rollTime)
                            .flatMap(message->{
                                ActiveAuction auction = createAuction(guild, channel, count, role, message, rollTime);
                                return activeAuctionRepository.saveAuction(auction)
                                    .then(Mono.fromRunnable(()->LOGGER.info("Auction {} created. Action: {}", auction, actionIdentity)).then());
                            }));
    }

    private Mono<Void> doInstantAuction(ChatInputInteractionEvent event, String actionIdentity, byte[] itemBytes, int count,
            Guild guild, MessageChannel channel, Role role, List<String> members){
        return  guildLocaleRepository.getGuildLocale(guild.getId().asLong())
                .map(GuildLocale::getLanguageTag)
                .flatMap(langTag->event.editReply()
                        .withContentOrNull(auctionPrepareText(langTag))
                        .flatMap(prepareMessage->kingdomRepository.getKingdomByRole(guild.getId().asLong(), role.getId().asLong())
                                .flatMap(kd->kingdomMemberRepository.getMembers(kd)
                                        .collectMap(member->member.getName().toLowerCase()))
                                .switchIfEmpty(Mono.fromSupplier(Collections::emptyMap))
                                .flatMapMany(memberMap->Flux.fromIterable(members)
                                        .map(nick->Optional.ofNullable(memberMap.get(nick.toLowerCase()))
                                                .map(member->new AuctionParticipant(Optional.of(member.getName()), Optional.ofNullable(member.getDiscordUserId())))
                                                .orElseGet(()->new AuctionParticipant(Optional.of(nick), Optional.empty()))))
                                .collectList()
                                .filter(list->!list.isEmpty())
                                .flatMap(participants->getParticipantsAndWinnersDisplays(participants, count))
                                .zipWhen(tuple->guildLocaleRepository.getGuildLocale(guild.getId().asLong()),
                                        (tuple, locale)->Tuples.of(tuple.getT1(), tuple.getT2(), locale))
                                .flatMap(tuple->event.createFollowup(role.getMention()+"\n"
                                            +(count > 1 ? auctionCountText(langTag, count)+"\n" : "")
                                            +auctionParticipantsText(langTag, tuple.getT1()))
                                        .withAllowedMentions(AllowedMentions.suppressAll())
                                        .withFiles(MessageCreateFields.File.of("item.png",
                                                new ByteArrayInputStream(ImageUtils.writeImage(ImageUtils.readImage(itemBytes), ImageFormat.JPEG))))
                                        .flatMap(message->channel.createMessage()
                                                .withMessageReference(message.getId())
                                                .withContent(auctionResultText(langTag, tuple.getT2()))))
                                )
                        )
                .then(Mono.fromRunnable(()->LOGGER.info("Instant auction for guildId {} and roleId {} completed. Action: {}",
                        guild.getId().asLong(), role.getId().asLong(), actionIdentity)).then());
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
                new ByteArrayInputStream(ImageUtils.writeImage(ImageUtils.readImage(itemBytes), ImageFormat.JPEG)));
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
                .flatMap(auction->activeAuctionRepository.deleteAuction(auction).then(Mono.just(auction)))
                .flatMap(auction->calculateAndSendActiveAuction(bot, auction)
                        .then(Mono.fromRunnable(()->LOGGER.info("Auction {} completed.", auction)).then()))
                .then();
    }

    private Mono<Void> calculateAndSendActiveAuction(DiscordBot bot, ActiveAuction auction){

        RestGuild guild = bot.getDiscordClient().getGuildById(Snowflake.of(auction.getGuildId()));

        RestChannel channel = bot.getDiscordClient().getChannelById(Snowflake.of(auction.getChannelId()));

        RestMessage message = channel.getRestMessage(Snowflake.of(auction.getMessageId()));

        return message.getData()
            .flatMap(messageData->Mono.justOrEmpty(messageData.reactions().toOptional()))
            .flatMapIterable(list->list)
            .flatMap(reaction->Mono.justOrEmpty(reaction.emoji().name().map(name->reaction.emoji().id()
                    .map(id->name+":"+id).orElse(name)))
                    .flatMapMany(reactionId->bot.getDiscordClient().getChannelService()
                            .getReactions(auction.getChannelId(), auction.getMessageId(), reactionId, reactionsQueryParams(false))
                            .mergeWith(bot.getDiscordClient().getChannelService()
                            .getReactions(auction.getChannelId(), auction.getMessageId(), reactionId, reactionsQueryParams(true)))))
            .map(userData->userData.id().asLong())
            .collect(Collectors.toSet())
            .zipWith(kingdomRepository.getKingdomByRole(guild.getId().asLong(), auction.getRoleId())
                    .map(Optional::of)
                    .switchIfEmpty(Mono.fromSupplier(Optional::empty)))
            .flatMapMany(tuple->Flux.fromIterable(tuple.getT1())
                    .flatMap(userId->guild.getMember(Snowflake.of(userId)))
                    .filter(member->auction.getRoleId() == 0 ? true : member.roles().stream().map(Id::asLong).anyMatch(roleId->auction.getRoleId() == roleId))
                    .flatMap(memberData->Mono.justOrEmpty(tuple.getT2())
                            .flatMap(kd->kingdomMemberRepository.findMembersByUser(kd, memberData.user().id().asLong())
                                    .take(1)
                                    .singleOrEmpty())
                            .map(Optional::of)
                            .switchIfEmpty(Mono.fromSupplier(Optional::empty))
                            .map(opt->Tuples.of(memberData, opt)))
                    .map(members->new AuctionParticipant(members.getT2().map(KingdomMember::getName), Optional.of(members.getT1().user().id().asLong()))))
            .collectList()
            .filter(list->!list.isEmpty())
            .flatMap(participants->getParticipantsAndWinnersDisplays(participants, auction.getCount()))
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
            .then(Mono.empty());
    }

    private Map<String, Object> reactionsQueryParams(boolean isSuper){
        Map<String, Object> params = new HashMap<>();
        params.put("limit", 100);
        if(isSuper) {
            params.put("type", 1);
        }
        return params;
    }

    private Mono<Tuple2<List<String>, List<String>>> getParticipantsAndWinnersDisplays(List<AuctionParticipant> participants, int lotsCount){

        return randomService.getRandomIntegers(0, participants.size(), lotsCount, true)
                .map(list->new HashSet<>(list))
                .map(idsSet->{
                    List<String> participantDisplayNames = new LinkedList<String>();
                    List<String> winnersDisplayNames = new LinkedList<String>();
                    for(int i = 0; i < participants.size(); ++i) {
                        AuctionParticipant member = participants.get(i);
                        if(idsSet.contains(i)) {
                            participantDisplayNames.add(String.format("> %d. %s", i+1, member));
                            winnersDisplayNames.add(String.format("> %s", member));
                        }else {
                            participantDisplayNames.add(String.format("%d. %s", i+1, member));
                        }
                    }
                    return Tuples.of(participantDisplayNames, winnersDisplayNames);
                });

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

    private String auctionCountText(String locale, int count) {
        return localization(locale, "message.auction.count", "count", Integer.toString(count));
    }

    private String auctionResultText(String locale, List<String> winners) {
        return localization(locale, "message.auction.results", "winners", String.join("\n", winners));
    }

    private String auctionResultNoParticipantsText(String locale, String roleMention) {
        return localization(locale, "message.auction.no_participants", "role", roleMention);
    }

    private String wrongAttachmentResponseText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.auction.wrong_attachment");
    }

    private String wrongTargetRoleResponseText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.auction.wrong_role");
    }

    private String wrongTimeResponseText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.auction.wrong_time");
    }

    private String onlyForChannelResponseText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.auction.only_in_channel");
    }

    private String onlyTimeOrMembersResponseText(DeferrableInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.auction.only_time_or_members");
    }

    private static class AuctionParticipant {

        final Optional<String> optNick;

        final Optional<Long> optUserId;

        AuctionParticipant(Optional<String> optNick, Optional<Long> optUserId) {
            this.optNick = optNick;
            this.optUserId = optUserId;
        }

        @Override
        public String toString() {
            return optNick.map(nick->optUserId.map(this::mention)
                        .map(mention->nick + " (" + mention + ")")
                        .orElse(nick))
                    .orElseGet(()->optUserId.map(this::mention)
                            .orElse(""));
        }

        String mention(long id) {
            return "<@"+id+">";
        }

        @Override
        public int hashCode() {
            return Objects.hash(optNick, optUserId);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AuctionParticipant other = (AuctionParticipant) obj;
            return Objects.equals(optNick, other.optNick) && Objects.equals(optUserId, other.optUserId);
        }


    }

}
