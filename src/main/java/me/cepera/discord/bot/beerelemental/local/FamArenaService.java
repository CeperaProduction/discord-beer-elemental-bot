package me.cepera.discord.bot.beerelemental.local;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.cepera.discord.bot.beerelemental.model.FamArenaBattle;
import me.cepera.discord.bot.beerelemental.repository.FamArenaBattleRepository;
import me.cepera.discord.bot.beerelemental.utils.ImageUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

public class FamArenaService {

    private static final Logger LOGGER = LogManager.getLogger(FamArenaService.class);

    private final FamArenaBattleRepository battleRepository;

    private final ImageToTextService imageToTextService;

    private final String rawNonDefaultLetterPattern = "[^a-zа-я0-9]";

    /*
    private final Pattern nonDefaultLetterReplacementPattern = Pattern.compile(rawNonDefaultLetterPattern,
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            */

    private final Pattern nonDefaultLetterContainsPattern = Pattern.compile(
            ".*"+rawNonDefaultLetterPattern+".*",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final Pattern simpleWordPattern = Pattern.compile("^(.*[^\\d]+.*)$");

    Path battleResulsFolder = Paths.get("data", "fam_arena", "results");

    private final Set<String> winResults = new HashSet<>(Arrays.asList("победа", "wictory"));
    private final Set<String> loseResults = new HashSet<>(Arrays.asList("поражение", "defeat"));

    @Inject
    public FamArenaService(FamArenaBattleRepository battleRepository, ImageToTextService imageToTextService) {
        this.battleRepository = battleRepository;
        this.imageToTextService = imageToTextService;
    }

    @Inject
    void init() {
        try {
            Files.createDirectories(battleResulsFolder);
        } catch (IOException e) {
            LOGGER.error("Can't create battle result images folder", e);
            throw new RuntimeException(e);
        }
    }

    /*
    private Tuple2<String, Pattern> getFixedSearchAndPattern(String search){
        Matcher searchFixMatcher = nonDefaultLetterReplacementPattern.matcher(search);
        String fixedSearch = searchFixMatcher.replaceAll("\\+");

        String rawFilterPattern = ".*"+fixedSearch.replace("+", rawNonDefaultLetterPattern)+".*";
        Pattern filterPattern = Pattern.compile(rawFilterPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        System.err.println("filter_pattern:" + rawFilterPattern);

        return Tuples.of(fixedSearch, filterPattern);
    }*/

    public Flux<String> getAllNicknames(long guildId){
        return battleRepository.findOpponentNicknames(guildId);
    }

    public Flux<String> getNicknameSuggestions(long guildId, String search){

        //Tuple2<String, Pattern> searchAndPattern = getFixedSearchAndPattern(search);

        return battleRepository.findOpponentNicknames(guildId, search);
                //.filter(nickname->searchAndPattern.getT2().matcher(nickname).matches());

    }

    public Mono<Tuple3<List<byte[]>, Boolean, Boolean>> findOpponentBattleResults(long guildId, String opponent, long minTimestamp,
            int offset, int count, Boolean winOnly){
        return battleRepository.findOpponentBattles(guildId, opponent, minTimestamp, offset, count+1, winOnly)
                .collectList()
                .map(list->{
                    if(list.isEmpty()) {
                        return Tuples.of(Collections.<FamArenaBattle>emptyList(), offset > 0, false);
                    }else if(list.size() > count) {
                        return Tuples.of(list.subList(0, count), offset > 0, true);
                    }else {
                        return Tuples.of(list, offset > 0, false);
                    }
                })
                .flatMap(tuple->Flux.fromIterable(tuple.getT1())
                        .flatMap(battle->readImage(battle.getImage()))
                        .collectList()
                        .map(images->Tuples.of(images, tuple.getT2(), tuple.getT3())));
    }

    public Mono<FamArenaBattle> storeBattleResult(long guildId, byte[] imageBytes){
        byte[] preparedImageBytes = compressImageTo1M(imageBytes);
        return imageToTextService.findAllWordPositions(preparedImageBytes)
                .filter(word->simpleWordPattern.matcher(word.getWord()).matches())
                .collectList()
                .flatMap(this::getBattlersAndResult)
                .flatMap(rawResult->storeBattleResult(guildId, preparedImageBytes, rawResult));
    }

    private Mono<FamArenaBattle> storeBattleResult(long guildId, byte[] imageBytes, Tuple3<WordPosition, WordPosition, Boolean> rawResult){
        FamArenaBattle battle = new FamArenaBattle();
        battle.setGuildId(guildId);
        battle.setBattler(rawResult.getT1().getWord());
        battle.setOpponent(rawResult.getT2().getWord());
        battle.setTimestamp(System.currentTimeMillis());
        battle.setWin(rawResult.getT3());
        battle.setAsiat(nonDefaultLetterContainsPattern.matcher(battle.getOpponent()).matches());

        BufferedImage image = ImageUtils.readImage(imageBytes);
        battle.setImage(generateImageName(battle));

        Tuple4<Integer, Integer, Integer, Integer> rect = calculateCutRectagle(image.getWidth(), image.getHeight(),
                rawResult.getT1(), rawResult.getT2());

        LOGGER.info("Calculated size for image {}: {}. Original size: {}x{}", battle.getImage(), rect, image.getWidth(), image.getHeight());

        return saveImage(battle.getImage(), ImageUtils.writeImagePng(
                ImageUtils.setMaxDimension(ImageUtils.getSubImage(image,
                        rect.getT1(), rect.getT2(), rect.getT3(), rect.getT4()), 850)))
                .then(battleRepository.addBattle(battle));
    }

    private byte[] compressImageTo1M(byte[] bytes) {
        int cap = 1024000;
        if(bytes.length > cap) {
            BufferedImage image = ImageUtils.readImage(bytes);
            if(1.0 * image.getWidth() / image.getHeight() > 1.65) {
                int x0 = (int) (image.getWidth() * 0.3);
                int width0 = image.getWidth() - x0 * 2;
                image = ImageUtils.getSubImage(image, x0, 0, width0, image.getHeight());
            }
            bytes = ImageUtils.writeImagePng(image);
            if(bytes.length > cap) {
                if(ImageUtils.getMaxDimension(image) > 1200) {
                    image = ImageUtils.setMaxDimension(image, 1200);
                    bytes = ImageUtils.writeImagePng(image);
                }
            }
        }
        return bytes;
    }

    private Mono<Tuple3<WordPosition, WordPosition, Boolean>> getBattlersAndResult(List<WordPosition> words){

        Optional<WordPosition> optVs = words.stream()
                .filter(word->word.getWord().equalsIgnoreCase("vs"))
                .findFirst();
        if(!optVs.isPresent()) {
            LOGGER.warn("Can't find 'vs' word");
            return Mono.empty();
        }
        WordPosition vs = optVs.get();
        int vsIndex = words.indexOf(vs);
        if(vsIndex < 1 || vsIndex > words.size()-2) {
            LOGGER.warn("Can't find battlers");
            return Mono.empty();
        }
        WordPosition battler = words.get(vsIndex-1);
        WordPosition opponent = words.get(vsIndex+1);

        WordPosition realOpponent = opponent;
        for(int i = vsIndex + 2; i < words.size(); ++i) {
            WordPosition potencial = words.get(i);
            int ydif = Math.abs(opponent.getY() - potencial.getY());
            int oheight = opponent.getHeight();
            if(ydif < oheight && potencial.getX() >= opponent.getX()) {
                realOpponent = new WordPosition(realOpponent.getWord()+potencial.getWord(), opponent.getX(), opponent.getY(),
                        realOpponent.getWidth() + potencial.getWidth() + (Math.abs(potencial.getX() - realOpponent.getX())),
                        Math.max(realOpponent.getHeight(), potencial.getHeight()));
            }
        }

        Boolean result = findResult(words);

        if(result == null) {
            LOGGER.warn("Can't determine result");
            return Mono.empty();
        }

        WordPosition finalOpponent = realOpponent;

        return Mono.fromSupplier(()->Tuples.of(battler, finalOpponent, result));
    }

    private Boolean findResult(List<WordPosition> words) {
        if(words.stream().anyMatch(w->winResults.contains(w.getWord().toLowerCase()))) {
            return true;
        }
        if(words.stream().anyMatch(w->loseResults.contains(w.getWord().toLowerCase()))) {
            return false;
        }
        return null;
    }

    private Tuple4<Integer, Integer, Integer, Integer> calculateCutRectagle(int sourceWidth, int sourceHeight,
            WordPosition battler, WordPosition opponent){

        int cx1 = battler.getX()+battler.getWidth();
        int cx2 = opponent.getX();
        int sectionWidth = (int) ((cx2 - cx1) * 2.9);
        int sectionHeight = (int) (sectionWidth * 1.15);
        int sectionX = opponent.getX() - (int)(sectionWidth * 0.665);
        int sectionY = battler.getY() - (int)(sectionHeight / 2.87);

        return Tuples.of(Math.max(sectionX, 0),
                Math.max(sectionY, 0),
                Math.max(Math.min(sectionWidth, sourceWidth-Math.max(sectionX, 0)), 0),
                Math.max(Math.min(sectionHeight, sourceHeight-Math.max(sectionY, 0)), 0));

    }

    private String generateImageName(FamArenaBattle battle) {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private Path battleResultsImagePath(String imageName) {
        return battleResulsFolder.resolve(imageName+".png");
    }

    private Mono<Void> saveImage(String name, byte[] imageBytes) {
        return Mono.fromCallable(()->Files.write(battleResultsImagePath(name), imageBytes)).then();
    }

    private Mono<byte[]> readImage(String name){
        return Mono.fromCallable(()->Files.readAllBytes(battleResultsImagePath(name)));
    }

    public Mono<byte[]> readImage(FamArenaBattle battle){
        return readImage(battle.getImage());
    }

}
