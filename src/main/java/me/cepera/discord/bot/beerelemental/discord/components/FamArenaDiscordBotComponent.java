package me.cepera.discord.bot.beerelemental.discord.components;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateFields;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.EmojiData;
import io.netty.util.internal.ThrowableUtil;
import me.cepera.discord.bot.beerelemental.discord.DiscordBot;
import me.cepera.discord.bot.beerelemental.discord.DiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.DiscordToolset;
import me.cepera.discord.bot.beerelemental.local.FamArenaService;
import me.cepera.discord.bot.beerelemental.local.ImageToTextService;
import me.cepera.discord.bot.beerelemental.local.PermissionService;
import me.cepera.discord.bot.beerelemental.local.lang.LanguageService;
import me.cepera.discord.bot.beerelemental.model.FamArenaBattle;
import me.cepera.discord.bot.beerelemental.model.Permission;
import me.cepera.discord.bot.beerelemental.utils.ImageFormat;
import me.cepera.discord.bot.beerelemental.utils.ImageUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public class FamArenaDiscordBotComponent implements DiscordBotComponent, DiscordToolset{

    private static final Logger LOGGER = LogManager.getLogger(FamArenaDiscordBotComponent.class);

    public static final String COMMAND_FAM_ARENA= "fams";
    public static final String COMMAND_FAM_SUB_SHOW = "show";
    public static final String COMMAND_FAM_SUB_ADD = "add";
    public static final String COMMAND_FAM_SUB_LIST = "list";
    public static final String COMMAND_OPTION_NICKNAME = "nickname";
    public static final String COMMAND_OPTION_PHOTO = "photo";
    public static final String COMMAND_OPTION_WON = "won";
    public static final String COMMAND_OPTION_RESULT = "result";
    public static final String COMMAND_OPTION_DAYS = "result";

    private static final int BATTLES_PER_PAGE = 10;

    private final FamArenaService famArenaService;

    private final ImageToTextService imageToTextService;

    private final LanguageService languageService;

    private final PermissionService permissionService;

    private final Map<String, ShowContext> showContexts = new ConcurrentHashMap<>();

    @Inject
    public FamArenaDiscordBotComponent(FamArenaService famArenaService, ImageToTextService imageToTextService,
            LanguageService languageService, PermissionService permissionService) {
        this.famArenaService = famArenaService;
        this.imageToTextService = imageToTextService;
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
                    .name(COMMAND_FAM_ARENA)
                    .nameLocalizationsOrNull(localization("command.fams"))
                    .description(localization(null, "command.fams.description"))
                    .descriptionLocalizationsOrNull(localization("command.fams.description"))
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_FAM_SUB_SHOW)
                            .nameLocalizationsOrNull(localization("command.fams.sub.show"))
                            .description(localization(null, "command.fams.sub.show.description"))
                            .descriptionLocalizationsOrNull(localization("command.fams.sub.show.description"))
                            .type(1)
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_OPTION_NICKNAME)
                                    .nameLocalizationsOrNull(localization("command.fams.option.nickname"))
                                    .description(localization(null, "command.fams.option.nickname.description"))
                                    .descriptionLocalizationsOrNull(localization("command.fams.option.nickname.description"))
                                    .required(false)
                                    .autocomplete(true)
                                    .type(3)
                                    .build())
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_OPTION_PHOTO)
                                    .nameLocalizationsOrNull(localization("command.fams.option.photo"))
                                    .description(localization(null, "command.fams.option.photo.description"))
                                    .descriptionLocalizationsOrNull(localization("command.fams.option.photo.description"))
                                    .required(false)
                                    .type(11)
                                    .build())
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_OPTION_WON)
                                    .nameLocalizationsOrNull(localization("command.fams.option.won"))
                                    .description(localization(null, "command.fams.option.won.description"))
                                    .descriptionLocalizationsOrNull(localization("command.fams.option.won.description"))
                                    .required(false)
                                    .type(5)
                                    .build())
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_OPTION_DAYS)
                                    .nameLocalizationsOrNull(localization("command.fams.option.days"))
                                    .description(localization(null, "command.fams.option.days.description"))
                                    .descriptionLocalizationsOrNull(localization("command.fams.option.days.description"))
                                    .required(false)
                                    .type(4)
                                    .minValue(1.0D)
                                    .build())
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_FAM_SUB_ADD)
                            .nameLocalizationsOrNull(localization("command.fams.sub.add"))
                            .description(localization(null, "command.fams.sub.add.description"))
                            .descriptionLocalizationsOrNull(localization("command.fams.sub.add.description"))
                            .type(1)
                            .addOption(ApplicationCommandOptionData.builder()
                                    .name(COMMAND_OPTION_RESULT)
                                    .nameLocalizationsOrNull(localization("command.fams.option.result"))
                                    .description(localization(null, "command.fams.option.result.description"))
                                    .descriptionLocalizationsOrNull(localization("command.fams.option.result.description"))
                                    .type(11)
                                    .required(true)
                                    .build())
                            .build())
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_FAM_SUB_LIST)
                            .nameLocalizationsOrNull(localization("command.fams.sub.list"))
                            .description(localization(null, "command.fams.sub.list.description"))
                            .descriptionLocalizationsOrNull(localization("command.fams.sub.list.description"))
                            .type(1)
                            .build())

                    .build());
        });
    }

    @Override
    public void configureGatewayClient(GatewayDiscordClient client) {

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

        if(!getCommandName(event).equals(COMMAND_FAM_ARENA)) {
            return Mono.empty();
        }

        Optional<ApplicationCommandInteractionOption> optSubCommand = getSubCommand(event);

        if(!optSubCommand.isPresent()) {
            return Mono.empty();
        }

        ApplicationCommandInteractionOption subCommand = optSubCommand.get();

        switch(subCommand.getName()) {
        case COMMAND_FAM_SUB_SHOW:

            Optional<String> optNickname = subCommand.getOption(COMMAND_OPTION_NICKNAME)
                    .flatMap(opt->opt.getValue()).map(value->value.asString());

            Optional<Attachment> optPhoto = subCommand.getOption(COMMAND_OPTION_PHOTO)
                    .flatMap(opt->opt.getValue()).map(value->value.asAttachment());

            Optional<Boolean> winFilter = subCommand.getOption(COMMAND_OPTION_WON)
                    .flatMap(opt->opt.getValue()).map(value->value.asBoolean());

            Optional<Long> optDays = subCommand.getOption(COMMAND_OPTION_DAYS)
                    .flatMap(opt->opt.getValue()).map(value->value.asLong());

            return handleFamArenaShowCommand(event, optNickname, optPhoto, winFilter, optDays);

        case COMMAND_FAM_SUB_ADD:

            Optional<Attachment> optResult = subCommand.getOption(COMMAND_OPTION_RESULT)
                    .flatMap(opt->opt.getValue()).map(value->value.asAttachment());

            return handleFamArenaAddCommand(event, optResult);

        case COMMAND_FAM_SUB_LIST:

            return handleFamArenaListCommand(event);

        default: return Mono.empty();
        }

    }

    @Override
    public Mono<Void> handleChatInputAutocompleteEvent(ChatInputAutoCompleteEvent event, DiscordBot bot) {
        if(!getCommandName(event).equals(COMMAND_FAM_ARENA)) {
            return Mono.empty();
        }

        Optional<ApplicationCommandInteractionOption> optSubCommand = getSubCommand(event.getOptions());

        if(!optSubCommand.isPresent()) {
            return Mono.empty();
        }

        ApplicationCommandInteractionOption subCommand = optSubCommand.get();

        if(subCommand.getName().equals(COMMAND_FAM_SUB_SHOW) && event.getFocusedOption().getName().equals(COMMAND_OPTION_NICKNAME)) {

            Optional<String> optValue = event.getFocusedOption().getValue().map(val->val.asString());

            return event.getInteraction().getGuild()
                    .flatMapMany(guild->{
                       if(optValue.isPresent() && !optValue.get().isEmpty()) {
                           return famArenaService.getNicknameSuggestions(guild.getId().asLong(), optValue.get());
                       }else {
                           return famArenaService.getAllNicknames(guild.getId().asLong());
                       }
                    })
                    .take(25)
                    .map(nick->ApplicationCommandOptionChoiceData.builder()
                            .name(nick)
                            .value(nick)
                            .build())
                    .cast(ApplicationCommandOptionChoiceData.class)
                    .collectList()
                    .flatMap(suggestions->event.respondWithSuggestions(suggestions));

        }

        return Mono.empty();

    }

    public Mono<Void> handleButtonInteractionEvent(ButtonInteractionEvent event){

        ShowContext context;

        if(event.getCustomId().startsWith("fams.show.")) {
            context = showContexts.remove(event.getCustomId().substring(10));
        }else {
            context = null;
        }

        if(context == null) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent(noButtonContextResponseText(event.getInteraction().getUserLocale()));
        }

        int offset;
        if(context.next) {
            offset = context.offset + BATTLES_PER_PAGE;
        }else {
            offset = Math.max(context.offset - BATTLES_PER_PAGE, 0);
        }

        return event.deferEdit()
                .then(event.getInteraction().getGuild())
                .filter(guild->guild.getId().asLong() == context.guildId)
                .flatMap(guild->handlePermissionCheck(guild,
                        hasPermission(event.getInteraction(), guild, Permission.FAM_ARENA_LOOKUP), Mono.empty()))
                .flatMap(guild->famArenaService.findOpponentBattleResults(context.guildId, context.opponent, context.timestamp, offset, BATTLES_PER_PAGE, context.winFilter)
                        .flatMap(result->sendBattleResults(event, guild, context.opponent, context.timestamp,
                                Optional.ofNullable(context.winFilter), result.getT1(), offset, result.getT2(), result.getT3())));

    }

    private Mono<Void> handleFamArenaShowCommand(ChatInputInteractionEvent event, Optional<String> optNickname,
            Optional<Attachment> optPhoto, Optional<Boolean> winFilter, Optional<Long> optDays){

        String actionIdentity = createActionIdentity(event, COMMAND_FAM_ARENA+"."+COMMAND_FAM_SUB_SHOW);

        long timestamp = optDays.map(this::startTimestamp).orElse(0L);

        return event.deferReply()
                .withEphemeral(true)
                .then(event.getInteraction().getGuild()
                        .switchIfEmpty(simpleEditReply(event, onlyForServerResponseText(event))))
                .flatMap(guild->handlePermissionCheck(guild,
                        hasPermission(event.getInteraction(), guild, Permission.FAM_ARENA_LOOKUP),
                        simpleEditReply(event, defaultNoPermissionText(event))))
                .zipWith(findNickname(optNickname, optPhoto)
                        .switchIfEmpty(simpleEditReply(event, cantFindAnyNicknameResponseText(event))))
                .flatMap(tuple->famArenaService.findOpponentBattleResults(tuple.getT1().getId().asLong(), tuple.getT2(),
                        timestamp, 0, BATTLES_PER_PAGE, winFilter.orElse(null))
                        .flatMap(result->sendBattleResults(event, tuple.getT1(), tuple.getT2(), timestamp, winFilter, result.getT1(), 0, result.getT2(), result.getT3())))
                .onErrorResume(e->{
                    LOGGER.error("Error while fetching battle results. Action: {} Error: {}", actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return simpleEditReply(event, defaultCommandErrorText(event));
                });


    }

    private Mono<Void> handleFamArenaAddCommand(ChatInputInteractionEvent event, Optional<Attachment> optResult){

        String actionIdentity = createActionIdentity(event, COMMAND_FAM_ARENA+"."+COMMAND_FAM_SUB_ADD);

        if(!optResult.isPresent()) {
            return replyError(event, this::wrongAttachmentResponseText, false);
        }

        Attachment result = optResult.get();

        String contentType = result.getContentType().orElse("");
        LOGGER.info("Received attachment with type {} for action {}", contentType, actionIdentity);
        if(!isImage(contentType)) {
            return replyError(event, this::wrongAttachmentResponseText, false);
        }

        return event.deferReply()
                .withEphemeral(true)
                .then(event.getInteraction().getGuild()
                        .switchIfEmpty(simpleEditReply(event, onlyForServerResponseText(event))))
                .flatMap(guild->handlePermissionCheck(guild,
                        hasPermission(event.getInteraction(), guild, Permission.FAM_ARENA_STORE),
                        simpleEditReply(event, defaultNoPermissionText(event))))
                .zipWith(getAttachmentContent(result)
                        .switchIfEmpty(simpleEditReply(event, cantGetAnyImageResponseText(event))))
                .flatMap(tuple->famArenaService.storeBattleResult(tuple.getT1().getId().asLong(), tuple.getT2())
                        .doOnNext(battle->LOGGER.info("Battle with {} stored in guild {}. Action: {}",
                                battle.getOpponent(), tuple.getT1().getId().asLong(), actionIdentity))
                        .switchIfEmpty(simpleEditReply(event, cantRecognizeStatsResponseText(event))))
                .flatMap(battle->famArenaService.readImage(battle)
                        .flatMap(image->sendAddBattleResult(event, battle, image)))
                .onErrorResume(e->{
                    LOGGER.error("Error while saving battle result. Action: {} Error: {}", actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return simpleEditReply(event, defaultCommandErrorText(event));
                });

    }

    private Mono<Void> handleFamArenaListCommand(ChatInputInteractionEvent event){

        String actionIdentity = createActionIdentity(event, COMMAND_FAM_ARENA+"."+COMMAND_FAM_SUB_LIST);

        return event.deferReply()
                .withEphemeral(true)
                .then(event.getInteraction().getGuild()
                        .switchIfEmpty(simpleEditReply(event, onlyForServerResponseText(event))))
                .flatMap(guild->handlePermissionCheck(guild,
                        hasPermission(event.getInteraction(), guild, Permission.FAM_ARENA_LOOKUP),
                        simpleEditReply(event, defaultNoPermissionText(event))))
                .flatMap(guild->famArenaService.getAllNicknames(guild.getId().asLong()).collectList())
                .flatMap(nicknames->simpleEditReply(event, availableTargets(event, nicknames)).then())
                .onErrorResume(e->{
                    LOGGER.error("Error while fetching available target nicknames. Action: {} Error: {}", actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return simpleEditReply(event, defaultCommandErrorText(event));
                });

    }

    private Mono<Void> sendAddBattleResult(ApplicationCommandInteractionEvent event, FamArenaBattle battle, byte[] image){

        MessageCreateFields.File file = MessageCreateFields.File.of("stats.jpg",
                new ByteArrayInputStream(ImageUtils.writeImage(ImageUtils.readImage(image), ImageFormat.JPEG)));

        return event.editReply()
                .withContentOrNull(battleResultStored(event, battle))
                .withFiles(file)
                .then();

    }

    private long startTimestamp(long daysOffset) {
        if(daysOffset > 0) {
            return System.currentTimeMillis() - daysOffset * 86400000;
        }
        return 0;
    }

    private Mono<Void> sendBattleResults(DeferrableInteractionEvent event, Guild guild, String opponent,
            long timestamp, Optional<Boolean> winFilter, List<byte[]> resultImages, int offset, boolean hasPrevious, boolean hasNext){

        Tuple3<String, List<MessageCreateFields.File>, List<LayoutComponent>> preparedResults = prepareBattleResults(
                event.getInteraction().getUserLocale(), guild, opponent, timestamp, winFilter, resultImages, offset, hasPrevious, hasNext);


        return event.editReply()
                .withContentOrNull(preparedResults.getT1())
                .withAttachmentsOrNull(null)
                .withFiles(preparedResults.getT2())
                .withComponentsOrNull(preparedResults.getT3())
                .then();
    }

    private Tuple3<String, List<MessageCreateFields.File>, List<LayoutComponent>> prepareBattleResults(String locale, Guild guild, String opponent,
            long timestamp, Optional<Boolean> winFilter, List<byte[]> resultImages, int offset, boolean hasPrevious, boolean hasNext){

        List<MessageCreateFields.File> files = new ArrayList<>();

        for(int i = 0; i < resultImages.size(); ++i) {
            MessageCreateFields.File file = MessageCreateFields.File.of(opponent+"_"+(i+1)+".jpg",
                    new ByteArrayInputStream(ImageUtils.writeImage(ImageUtils.readImage(resultImages.get(i)), ImageFormat.JPEG)));
            files.add(file);
        }

        List<LayoutComponent> components = new ArrayList<>();

        if(hasPrevious || hasNext) {

            ShowContext prevContext = null;
            ShowContext nextContext = null;
            if(hasPrevious) {
                prevContext = createShowContext(guild, opponent, timestamp, winFilter, offset, false);
            }
            if(hasNext) {
                nextContext = createShowContext(guild, opponent, timestamp, winFilter, offset, true);
            }

            Button prevButton = Button.primary("fams.show."+prevContext, ReactionEmoji.of(EmojiData.builder()
                    .name("\u2b05\ufe0f")
                    .build()))
                    .disabled(!hasPrevious);

            Button nextButton = Button.primary("fams.show."+nextContext, ReactionEmoji.of(EmojiData.builder()
                    .name("\u27a1\ufe0f")
                    .build()))
                    .disabled(!hasNext);

            components.add(ActionRow.of(prevButton, nextButton));

        }

        return Tuples.of(battleResults(locale, opponent, timestamp, winFilter, files.isEmpty()), files, components);

    }

    private Mono<String> findNickname(Optional<String> optNickname, Optional<Attachment> optPhoto){
        if(optNickname.isPresent()) {
            return Mono.fromSupplier(optNickname::get);
        }
        return Mono.justOrEmpty(optPhoto)
                .map(this::getAttachmentContentUrl)
                .flatMap(url->imageToTextService.findNicknames(url)
                        .take(1)
                        .singleOrEmpty());
    }

    private boolean isImage(String contentType) {
        return contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg");
    }

    private String battleResults(String locale, String opponent, long timestamp, Optional<Boolean> winFilter, boolean empty) {
        String msg = localization(locale, "message.fams.results", "opponent", opponent);
        if(winFilter.isPresent()) {
            msg += "\n" + localization(locale, "message.fams.results."+(winFilter.get() ? "wins_only" : "loses_only"));
        }
        if(timestamp > 0) {
            msg += "\n" + localization(locale, "message.fams.results.starting_from", "timestamp", "<t:"+(timestamp/1000)+":R>");
        }
        if(empty) {
            msg += "\n"+localization(locale, "message.fams.no_battles_found");
        }
        return msg;
    }

    private String battleResultStored(ApplicationCommandInteractionEvent event, FamArenaBattle battle) {
        return localization(event.getInteraction().getUserLocale(), "message.fams.stored", "opponent", battle.getOpponent(),
                "result", localization(event.getInteraction().getUserLocale(), "message.fams.result."+(battle.isWin() ? "win" : "lose")));
    }

    private String availableTargets(ApplicationCommandInteractionEvent event, List<String> nicknames) {
        String nicknamesStr = String.join(" ", nicknames);
        if(nicknamesStr.isEmpty()) {
            nicknamesStr = "<none>";
        }
        return localization(event.getInteraction().getUserLocale(), "message.fams.targets", "nicknames", "```"+nicknamesStr+"```");
    }

    private String wrongAttachmentResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.fams.wrong_attachment");
    }

    private String onlyForServerResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.fams.only_in_channel");
    }

    private String cantGetAnyImageResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.fams.cant_receive_any_image");
    }

    private String cantFindAnyNicknameResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.fams.cant_find_any_nickname");
    }

    private String cantRecognizeStatsResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.fams.cant_recognize_stats");
    }

    private String noButtonContextResponseText(String locale) {
        return localization(locale, "message.fams.no_button_context");
    }

    private ShowContext createShowContext(Guild guild, String opponent, long timestamp, Optional<Boolean> winFilter, int offset, boolean next) {
        String id = UUID.randomUUID().toString().replace("-", "");
        ShowContext context = new ShowContext(id, guild.getId().asLong(), opponent, timestamp, winFilter.orElse(null), offset, next);
        showContexts.put(id, context);
        return context;
    }

    private static class ShowContext {

        final String id;

        final long guildId;

        final String opponent;

        final long timestamp;

        final Boolean winFilter;

        final int offset;

        final boolean next;

        ShowContext(String id, long guildId, String opponent, long timestamp, Boolean winFilter, int offset, boolean next) {
            this.id = id;
            this.guildId = guildId;
            this.opponent = opponent;
            this.timestamp = timestamp;
            this.winFilter = winFilter;
            this.offset = offset;
            this.next = next;
        }

        @Override
        public String toString() {
            return id;
        }

    }

}
