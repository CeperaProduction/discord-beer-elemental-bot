package me.cepera.discord.bot.beerelemental.local;

import me.cepera.discord.bot.beerelemental.utils.ImageFormat;
import reactor.core.publisher.Flux;

public interface ImageToTextService {

    Flux<String> findAllWords(byte[] imageBytes, ImageFormat format);

    Flux<String> findUniqueWords(byte[] imageBytes, ImageFormat format);

    Flux<String> findNicknames(byte[] imageBytes, ImageFormat format);

    Flux<String> findAllWords(String imageUrl);

    Flux<String> findUniqueWords(String imageUrl);

    Flux<String> findNicknames(String imageUrl);

    Flux<WordPosition> findAllWordPositions(byte[] imageBytes, ImageFormat format);

    Flux<WordPosition> findAllWordPositions(String imageUrl);

}
