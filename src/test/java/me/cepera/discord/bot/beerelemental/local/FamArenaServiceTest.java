package me.cepera.discord.bot.beerelemental.local;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import me.cepera.discord.bot.beerelemental.config.OCRConfig;
import me.cepera.discord.bot.beerelemental.config.OCRReplaceRule;
import me.cepera.discord.bot.beerelemental.converter.GenericJsonBodyConverter;
import me.cepera.discord.bot.beerelemental.dto.ocr.OCRResponseDto;
import me.cepera.discord.bot.beerelemental.model.FamArenaBattle;
import me.cepera.discord.bot.beerelemental.remote.OCRRemoteService;
import me.cepera.discord.bot.beerelemental.repository.sqlite.SQLiteFamArenaRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.db.SQLiteDatabase;

@Disabled
public class FamArenaServiceTest {

    static OCRService ocrService;

    static FamArenaService famArenaService;

    @BeforeAll
    static void prepare() {
        OCRConfig cfg = new OCRConfig();

        cfg.setKey("<key>");

        cfg.getNickSettings().getIgnore().addAll(Arrays.asList(

              ".",
              "[^a-zа-я]+",
              "Рейтинг",
              "Имя",
              "Убийства",
              "Помощи",
              "Счет",
              "Смерти"

                ));

        cfg.getNickSettings().getReplace().addAll(Arrays.asList(

                new OCRReplaceRule("×", "x")

                  ));

        ocrService = new OCRService(new OCRRemoteService(cfg),
                new GenericJsonBodyConverter<>(OCRResponseDto.class),
                cfg);

        famArenaService = new FamArenaService(new SQLiteFamArenaRepository(
                new SQLiteDatabase(Paths.get("target", "test", "data", "fam_arena_test.sqlite"))), ocrService);

        famArenaService.battleResulsFolder = Paths.get("target", "test", "data", "fam_arena_results");

        famArenaService.init();
    }

    @Test
    void testResolveBattle() throws IOException {

        byte[] imageBytes = loadImage("test_image4.png");

        FamArenaBattle battle = famArenaService.storeBattleResult(1, imageBytes).block();

        System.err.println("battle: "+battle);

        assertNotNull(battle);

    }

    private byte[] loadImage(String name) throws IOException {
        try(InputStream is = FamArenaServiceTest.class.getClassLoader().getResourceAsStream(name)){
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int readen = 0;
            while((readen = is.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, readen);
            }
            return out.toByteArray();
        }
    }

}
