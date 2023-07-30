package me.cepera.discord.bot.beerelemental.local;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.cepera.discord.bot.beerelemental.config.RandomConfig;
import me.cepera.discord.bot.beerelemental.converter.BodyConverter;
import me.cepera.discord.bot.beerelemental.dto.random.GenerateIntegersParamsDto;
import me.cepera.discord.bot.beerelemental.dto.random.RandomIntegersResponseDto;
import me.cepera.discord.bot.beerelemental.dto.random.RandomRequestDto;
import me.cepera.discord.bot.beerelemental.remote.RandomOrgRemoteService;
import reactor.core.publisher.Mono;

public class RandomOrgService implements RandomService{

    private static final Logger LOGGER = LogManager.getLogger(RandomOrgService.class);

    private final RandomOrgRemoteService remote;

    private final RandomConfig config;

    private final BodyConverter<RandomRequestDto> requestConverter;

    private final BodyConverter<RandomIntegersResponseDto> responseConverter;

    @Inject
    public RandomOrgService(RandomOrgRemoteService remote, RandomConfig config,
            BodyConverter<RandomRequestDto> requestConverter,
            BodyConverter<RandomIntegersResponseDto> responseConverter) {
        this.remote = remote;
        this.config = config;
        this.requestConverter = requestConverter;
        this.responseConverter = responseConverter;
    }

    @Override
    public Mono<List<Integer>> getRandomIntegers(int min, int max, int maxCount, boolean unique) {
        int realCount = Math.max(Math.min(max-min, maxCount), 0);
        if(realCount == 0) {
            return Mono.fromSupplier(Collections::emptyList);
        }
        if(config.isRemote()) {
            return generateRandomIntegersRemote(min, max, realCount, unique)
                    .onErrorResume(e->{
                        LOGGER.error("Error while generating random numbers using remote service. Generate them localy.", e);
                        return generateRandomIntegersLocaly(min, max, realCount, unique);
                    });
        }else {
            return generateRandomIntegersLocaly(min, max, realCount, unique);
        }
    }

    private Mono<List<Integer>> generateRandomIntegersRemote(int min, int max, int count, boolean unique){
        RandomRequestDto request = new RandomRequestDto();
        request.setMethod("generateIntegers");

        GenerateIntegersParamsDto params = new GenerateIntegersParamsDto();
        params.setApiKey(config.getKey());
        params.setMin(min);
        params.setMax(max-1);
        params.setReplacement(!unique);
        params.setN(count);

        request.setParams(params);

        return requestConverter.write(request)
                .flatMap(remote::basicApiCall)
                .flatMap(responseConverter::read)
                .map(response->{
                    if(response.getError() != null) {
                        throw new IllegalStateException("An error has occurred on remote service: "+response.getError());
                    }
                    LOGGER.info("Metadata received from random.org - bitsUsed: {}, bitsLeft: {}, requestsLeft: {}, advisoryDelay: {}",
                            response.getResult().getBitsUsed(), response.getResult().getBitsLeft(),
                            response.getResult().getRequestsLeft(), response.getResult().getAdvisoryDelay());
                    return response.getResult().getRandom().getData();
                });

    }

    private Mono<List<Integer>> generateRandomIntegersLocaly(int min, int max, int count, boolean unique){
        return Mono.fromSupplier(()->{
            Random rand = new Random();
            if(unique) {
                List<Integer> line = new LinkedList<>(IntStream.range(min, max).boxed().collect(Collectors.toList()));
                return IntStream.range(0, count).map(i->line.remove(rand.nextInt(line.size())))
                        .boxed().collect(Collectors.toList());
            }else {
                return IntStream.range(0, count).map(i->min+rand.nextInt(max-min))
                        .boxed().collect(Collectors.toList());
            }
        });
    }

}
