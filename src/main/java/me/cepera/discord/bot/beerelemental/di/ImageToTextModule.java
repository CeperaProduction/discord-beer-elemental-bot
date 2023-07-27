package me.cepera.discord.bot.beerelemental.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.cepera.discord.bot.beerelemental.converter.BodyConverter;
import me.cepera.discord.bot.beerelemental.converter.GenericJsonBodyConverter;
import me.cepera.discord.bot.beerelemental.dto.ocr.OCRResponseDto;
import me.cepera.discord.bot.beerelemental.local.ImageToTextService;
import me.cepera.discord.bot.beerelemental.local.OCRService;

@Module
public class ImageToTextModule {

    @Provides
    @Singleton
    ImageToTextService imageToTextModule(OCRService ocrService) {
        return ocrService;
    }

    @Provides
    BodyConverter<OCRResponseDto> ocrResponseDtoBodyConverter(){
        return new GenericJsonBodyConverter<>(OCRResponseDto.class);
    }

}
