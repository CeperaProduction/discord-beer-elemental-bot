package me.cepera.discord.bot.beerelemental.remote;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import reactor.core.publisher.Mono;

public class RandomOrgRemoteService implements RemoteService{

    private final URI apiUri = URI.create("https://api.random.org/json-rpc/4/invoke");

    @Inject
    public RandomOrgRemoteService() {

    }

    public Mono<byte[]> basicApiCall(byte[] body){
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return post(apiUri, headers, body);
    }

}
