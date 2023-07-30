package me.cepera.discord.bot.beerelemental.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.cepera.discord.bot.beerelemental.converter.BodyConverter;
import me.cepera.discord.bot.beerelemental.converter.GenericJsonBodyConverter;
import me.cepera.discord.bot.beerelemental.dto.random.RandomIntegersResponseDto;
import me.cepera.discord.bot.beerelemental.dto.random.RandomRequestDto;
import me.cepera.discord.bot.beerelemental.local.RandomOrgService;
import me.cepera.discord.bot.beerelemental.local.RandomService;

@Module
public class RandomModule {

    @Provides
    @Singleton
    RandomService randomService(RandomOrgService service) {
        return service;
    }

    @Provides
    BodyConverter<RandomRequestDto> randomRequestDtoboBodyConverter(){
        return new GenericJsonBodyConverter<RandomRequestDto>(RandomRequestDto.class);
    }

    @Provides
    BodyConverter<RandomIntegersResponseDto> randomIntegersResponseDtoBodyConverter(){
        return new GenericJsonBodyConverter<RandomIntegersResponseDto>(RandomIntegersResponseDto.class);
    }

}
