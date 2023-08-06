package me.cepera.discord.bot.beerelemental.local;

import reactor.core.publisher.Flux;

public interface ImageToTextService {

    Flux<String> findAllWords(byte[] imageBytes);

    Flux<String> findUniqueWords(byte[] imageBytes);

    Flux<String> findNicknames(byte[] imageBytes);

    Flux<String> findAllWords(String imageUrl);

    Flux<String> findUniqueWords(String imageUrl);

    Flux<String> findNicknames(String imageUrl);

    Flux<WordPosition> findAllWordPositions(byte[] imageBytes);

    Flux<WordPosition> findAllWordPositions(String imageUrl);

}
