package me.cepera.discord.bot.beerelemental.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import reactor.core.publisher.Mono;

public class TimeUtils {

    private static final Pattern TIME_PATTERN = Pattern.compile("((?<hours>\\d+)h)?\\s*((?<minutes>\\d+)m)?", Pattern.CASE_INSENSITIVE);

    public static Mono<Instant> getInstant(String offsetFromNow) {
        return Mono.fromSupplier(()->{
            Matcher matcher = TIME_PATTERN.matcher(offsetFromNow);
            if(matcher.matches()) {
                String hoursString = matcher.group("hours");
                String minutesString = matcher.group("minutes");
                long minutes = 0;
                if(hoursString != null) {
                    minutes += Long.parseLong(hoursString) * 60;
                }
                if(minutesString != null) {
                    minutes += Long.parseLong(minutesString);
                }
                if(minutes > 0) {
                    return Instant.now().plus(minutes, ChronoUnit.MINUTES);
                }
            }
            throw new IllegalArgumentException("Bad time duration format: "+offsetFromNow);
        });
    }

}
