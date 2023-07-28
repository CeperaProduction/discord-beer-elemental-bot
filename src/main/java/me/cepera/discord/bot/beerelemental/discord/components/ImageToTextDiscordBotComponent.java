package me.cepera.discord.bot.beerelemental.discord.components;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateFields;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import io.netty.util.internal.ThrowableUtil;
import me.cepera.discord.bot.beerelemental.discord.DiscordBotComponent;
import me.cepera.discord.bot.beerelemental.discord.DiscordToolset;
import me.cepera.discord.bot.beerelemental.local.ImageToTextService;
import me.cepera.discord.bot.beerelemental.local.lang.LanguageService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ImageToTextDiscordBotComponent implements DiscordBotComponent, DiscordToolset {

    private static final Logger LOGGER = LogManager.getLogger(ImageToTextDiscordBotComponent.class);

    public static final String COMMAND_NICKNAMES= "nicknames";
    public static final String COMMAND_OPTION_IMAGE = "image";
    public static final String COMMAND_OPTION_EXPANDED = "expanded";

    public static final String MESSAGE_COMMAND_NICKNAMES= "nicknames";

    private final LanguageService languageService;

    private final ImageToTextService imageToTextService;

    @Inject
    public ImageToTextDiscordBotComponent(LanguageService languageService, ImageToTextService imageToTextService) {
        this.languageService = languageService;
        this.imageToTextService = imageToTextService;
    }

    @Override
    public LanguageService languageService() {
        return languageService;
    }

    @Override
    public Flux<ApplicationCommandRequest> commandsToRegister() {
        return Flux.create(sink->{
            sink.next(ApplicationCommandRequest.builder()
                    .name(COMMAND_NICKNAMES)
                    .nameLocalizationsOrNull(localization("command.nicknames"))
                    .description(localization(null, "command.nicknames.description"))
                    .descriptionLocalizationsOrNull(localization("command.nicknames.description"))
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_IMAGE+1)
                            .nameLocalizationsOrNull(addIndexSuffix(localization("command.nicknames.option.image"), 1))
                            .description(localization(null, "command.nicknames.option.image.description"))
                            .descriptionLocalizationsOrNull(localization("command.nicknames.option.image.description"))
                            .required(true)
                            .type(11)
                            .build())
                    .addAllOptions(IntStream.range(2, 11).mapToObj(i->ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_IMAGE+i)
                            .nameLocalizationsOrNull(addIndexSuffix(localization("command.nicknames.option.image"), i))
                            .description(localization(null, "command.nicknames.option.image.description"))
                            .descriptionLocalizationsOrNull(localization("command.nicknames.option.image.description"))
                            .required(false)
                            .type(11)
                            .build())
                            .collect(Collectors.toList()))
                    .addOption(ApplicationCommandOptionData.builder()
                            .name(COMMAND_OPTION_EXPANDED)
                            .nameLocalizationsOrNull(localization("command.nicknames.option.expanded"))
                            .description(localization(null, "command.nicknames.option.expanded.description"))
                            .descriptionLocalizationsOrNull(localization("command.nicknames.option.expanded.description"))
                            .required(false)
                            .type(5)
                            .build())
                    .build());

            sink.next(ApplicationCommandRequest.builder()
                    .name(MESSAGE_COMMAND_NICKNAMES)
                    .nameLocalizationsOrNull(localization("command.message.nicknames"))
                    .type(3)
                    .build());

        });
    }

    private Map<String, String> addIndexSuffix(Map<String, String> original, int index){
        Map<String, String> newMap = new HashMap<>();
        original.forEach((key, value)->newMap.put(key, value+index));
        return newMap;
    }

    @Override
    public Mono<Void> handleChatInputInteractionEvent(ChatInputInteractionEvent event) {
        if(!getCommandName(event).equals(COMMAND_NICKNAMES)) {
            return Mono.empty();
        }

        List<Attachment> attachments = IntStream.range(1, 11)
                .mapToObj(i->event.getOption(COMMAND_OPTION_IMAGE+i))
                .map(opt->opt.flatMap(option->option.getValue()).map(value->value.asAttachment()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        boolean expanded = event.getOption(COMMAND_OPTION_EXPANDED)
                .flatMap(option->option.getValue())
                .map(value->value.asBoolean())
                .orElse(false);

        return handleNicknamesCommand(event, attachments, expanded, Optional.empty());

    }

    @Override
    public Mono<Void> handleMessageInteractionEvent(MessageInteractionEvent event) {
        if(!getCommandName(event).equals(MESSAGE_COMMAND_NICKNAMES)) {
            return Mono.empty();
        }

        return event.getTargetMessage()
                .flatMap(message->handleNicknamesCommand(event, message.getAttachments(), true, Optional.of(createMessageUrl(message))));
    }

    private Mono<Void> handleNicknamesCommand(ApplicationCommandInteractionEvent event, List<Attachment> attachments, boolean expanded,
            Optional<String> messageUrl){

        String actionIdentity = createActionIdentity(event, "nicknames");

        List<Attachment> attachedImages = attachments.stream().filter(this::isImage).collect(Collectors.toList());

        return event.deferReply()
                .withEphemeral(false)
                .thenMany(getAttachmentContentUrls(attachments))
                .switchIfEmpty(event.editReply(cantGetAnyImageResponseText(event)).then(Mono.empty()))
                .doOnNext(bytes->LOGGER.info("Start searching for nicknames on image in action {}", actionIdentity))
                .flatMap(imageToTextService::findNicknames)
                .collectList()
                .map(list->new ArrayList<>(new LinkedHashSet<>(list)))
                .flatMap(nicknames->sendFoundNicknames(event, nicknames, expanded, messageUrl.isPresent() ? Collections.emptyList() : attachedImages, messageUrl))
                .onErrorResume(e->{
                    LOGGER.error("Error during resolving nicknames from images. Action: {} Error: {}", actionIdentity, ThrowableUtil.stackTraceToString(e));
                    return replyError(event, this::defaultCommandErrorText, true);
                });

    }

    private boolean isImage(Attachment attachment) {
        return attachment.getContentType().filter(this::isImage).isPresent();
    }

    private Flux<String> getAttachmentContentUrls(List<Attachment> attachments){
        return Flux.fromIterable(attachments)
                .map(this::getAttachmentContentUrl);
    }

    private boolean isImage(String contentType) {
        return contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg");
    }

    private Mono<Void> sendFoundNicknames(ApplicationCommandInteractionEvent event, List<String> nicknames, boolean expanded,
            List<Attachment> attachments, Optional<String> messageUrl){
        if(nicknames.isEmpty()) {
            return event.editReply()
                    .withContentOrNull(nothingWasFoundResponseText(event))
                    .then();
        }

        String content;
        if(expanded) {
            content = ">>> "+String.join("\n", IntStream.range(0, nicknames.size())
                    .mapToObj(i->(i+1)+". "+nicknames.get(i))
                    .collect(Collectors.toList()));
        }else {
            content = "```"+String.join(" ", nicknames)+"```";
        }

        if(messageUrl.isPresent()) {
            content = messageUrl.get() + "\n" + content;
        }

        String finalContent = content;

        return Flux.fromIterable(attachments)
                .flatMap(this::getAttachmentContent)
                .collectList()
                .flatMapMany(attachmentBodies->Flux.range(0, attachmentBodies.size())
                        .map(i->MessageCreateFields.File.of("image"+(i+1)+".png", new ByteArrayInputStream(attachmentBodies.get(i)))))
                .collectList()
                .flatMap(imageFiles->event.editReply(finalContent)
                    .withFiles(imageFiles)
                    .then());
    }

    private String createMessageUrl(Message message) {
        return String.format("https://discord.com/channels/%s/%s/%s",
                message.getGuildId().map(id->Long.toString(id.asLong())).orElse("@me"),
                message.getChannelId().asLong(),
                message.getId().asLong());
    }

    private String cantGetAnyImageResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.nicknames.cant_receive_any_image");
    }

    private String nothingWasFoundResponseText(ApplicationCommandInteractionEvent event) {
        return localization(event.getInteraction().getUserLocale(), "message.nicknames.nothing_was_found");
    }

}
