package me.cepera.discord.bot.beerelemental.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.util.internal.ThrowableUtil;
import me.cepera.discord.bot.beerelemental.config.OCRConfig;
import me.cepera.discord.bot.beerelemental.converter.BodyConverter;
import me.cepera.discord.bot.beerelemental.dto.ocr.OCRResponseDto;
import me.cepera.discord.bot.beerelemental.remote.OCRRemoteService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class OCRService implements ImageToTextService{

    private static final Logger LOGGER = LogManager.getLogger(OCRService.class);

    private final OCRRemoteService remote;

    private final BodyConverter<OCRResponseDto> responseConverter;

    private final Scheduler requestsScheduler = Schedulers.newParallel("ocr-requests-scheduler", 4, true);

    private final List<Pattern> nicknameIgnorePatterns = new ArrayList<>();

    private final List<Tuple2<Pattern, String>> replacements = new ArrayList<>();

    @Inject
    public OCRService(OCRRemoteService remote, BodyConverter<OCRResponseDto> responseConverter, OCRConfig config) {
        this.remote = remote;
        this.responseConverter = responseConverter;
        config.getNickSettings().getIgnore().forEach(patternStr->tryCompilePattern(patternStr)
                .ifPresent(nicknameIgnorePatterns::add));
        config.getNickSettings().getReplace().forEach(replace->tryCompilePattern(replace.getFrom())
                .ifPresent(pattern->replacements.add(Tuples.of(pattern, replace.getTo()))));
    }

    private Optional<Pattern> tryCompilePattern(String patternStr){
        try {
            return Optional.of(Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        }catch (PatternSyntaxException e) {
            LOGGER.error("Bad pattern '{}': {}", patternStr, ThrowableUtil.stackTraceToString(e));
            return Optional.empty();
        }
    }

    @Override
    public Flux<String> findAllWords(byte[] imageBytes){
        return image2textRequest("image.png", "image/png", imageBytes, params())
                .flatMap(responseConverter::read)
                .doOnNext(this::logResult)
                .flatMapIterable(OCRResponseDto::getParsedResults)
                .flatMapIterable(result->splitWords(result.getParsedText()));
    }

    @Override
    public Flux<String> findAllWords(String imageUrl) {
        return image2textRequest(imageUrl, params())
                .flatMap(responseConverter::read)
                .doOnNext(this::logResult)
                .flatMapIterable(OCRResponseDto::getParsedResults)
                .flatMapIterable(result->splitWords(result.getParsedText()));
    }

    private Map<String, Object> params(){
        Map<String, Object> params = new HashMap<>();

        params.put("OCREngine", 2);
        params.put("isTable", true);
        params.put("scale", true);
        params.put("isOverlayRequired", false);

        return params;
    }

    private void logResult(OCRResponseDto response) {
        if(response.getProcessingTimeInMilliseconds() != null) {
            LOGGER.info("OCR ended image processing for {} seconds",
                    String.format("%1.2f", 1.0D * response.getProcessingTimeInMilliseconds() / 1000));
        }
        if(response.getIsErroredOnProcessing() != null && response.getIsErroredOnProcessing()) {
            LOGGER.error("Errors were detected during OCR processing. OCR response object: {}", response);
        }
    }

    @Override
    public Flux<String> findUniqueWords(byte[] imageBytes){
        return findAllWords(imageBytes)
                .collectList()
                .flatMapIterable(list->new LinkedHashSet<>(list));
    }

    @Override
    public Flux<String> findUniqueWords(String imageUrl) {
        return findAllWords(imageUrl)
                .collectList()
                .flatMapIterable(list->new LinkedHashSet<>(list));
    }

    @Override
    public Flux<String> findNicknames(byte[] imageBytes){
        return findUniqueWords(imageBytes)
                .map(this::applyReplacements)
                .filter(this::filterNick);
    }

    @Override
    public Flux<String> findNicknames(String imageUrl) {
        return findUniqueWords(imageUrl)
                .map(this::applyReplacements)
                .filter(this::filterNick);
    }

    private String applyReplacements(String word) {
        for(Tuple2<Pattern, String> replacement : this.replacements) {
            word = replacement.getT1().matcher(word).replaceAll(replacement.getT2());
        }
        return word;
    }

    private boolean filterNick(String nicknameCandidate) {
        return !nicknameIgnorePatterns.stream()
                .anyMatch(pattern->pattern.matcher(nicknameCandidate).matches());
    }

    private Mono<byte[]> image2textRequest(String fileName, String fileContentType, byte[] fileContent,
            Map<String, Object> formData){
        return Mono.just(0)
                .publishOn(requestsScheduler)
                .flatMap(z->remote.image2text(fileName, fileContentType, fileContent, formData))
                .publishOn(Schedulers.boundedElastic());
    }

    private Mono<byte[]> image2textRequest(String imageUrl, Map<String, Object> formData){
        return Mono.just(0)
                .publishOn(requestsScheduler)
                .flatMap(z->remote.image2text(imageUrl, formData))
                .publishOn(Schedulers.boundedElastic());
    }

    private List<String> splitWords(String string) {
        return Arrays.stream(string.trim().split("\n"))
                .map(String::trim)
                .flatMap(wordCandidate->Arrays.stream(wordCandidate.split("\\s")))
                .filter(word->!word.isEmpty())
                .collect(Collectors.toList());
    }



}
