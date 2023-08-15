package me.cepera.discord.bot.beerelemental.repository;

import me.cepera.discord.bot.beerelemental.model.Kingdom;
import me.cepera.discord.bot.beerelemental.model.KingdomMember;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KingdomMemberRepository {

    Flux<KingdomMember> getMembers(Kingdom kingdom);

    Flux<KingdomMember> findMembersByUser(long guildId, long userId);

    Flux<KingdomMember> findMembersByUser(Kingdom kingdom, long userId);

    Flux<KingdomMember> findMembersByNickname(long guildId, String nickname);

    Mono<KingdomMember> findMemberByNickname(Kingdom kingdom, String nickname);


    Mono<Void> dropReceivedWolfs(Kingdom kingdom);

    Mono<KingdomMember> saveMember(KingdomMember member);

    Mono<Void> deleteMember(KingdomMember member);



}
