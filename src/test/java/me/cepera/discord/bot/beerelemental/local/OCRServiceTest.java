package me.cepera.discord.bot.beerelemental.local;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import me.cepera.discord.bot.beerelemental.config.OCRConfig;
import me.cepera.discord.bot.beerelemental.config.OCRReplaceRule;
import me.cepera.discord.bot.beerelemental.converter.GenericJsonBodyConverter;
import me.cepera.discord.bot.beerelemental.dto.ocr.OCRResponseDto;
import me.cepera.discord.bot.beerelemental.remote.OCRRemoteService;

@Disabled
public class OCRServiceTest {

    @Test
    void testFindNicknames() throws IOException {

        byte[] imageBytes = loadImage("test_image.png");

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

        OCRService service = new OCRService(new OCRRemoteService(cfg),
                new GenericJsonBodyConverter<>(OCRResponseDto.class),
                cfg);

        List<String> nicknames = service.findNicknames(imageBytes).collectList().block();

        System.err.println("nicknames: "+nicknames);

        assertFalse(nicknames.isEmpty(), "No nicknames were received");

    }

    private byte[] loadImage(String name) throws IOException {
        try(InputStream is = OCRServiceTest.class.getClassLoader().getResourceAsStream(name)){
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
