package me.cepera.discord.bot.beerelemental.remote;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.handler.codec.http.HttpHeaderNames;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientForm;
import reactor.netty.http.client.HttpClientResponse;

public interface RemoteService {

    Logger LOGGER = LogManager.getLogger(RemoteService.class);

    HttpClient DEFAULT_HTTP_CLIENT = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(30));

    default HttpClient httpClient() {
        return DEFAULT_HTTP_CLIENT;
    }

    default Mono<byte[]> get(URI uri){
        return get(uri, Collections.emptyMap());
    }

    default Mono<byte[]> get(URI uri, Map<String, String> headersMap){
        return httpClient()
                .headers(headers->headersMap.forEach((key, value)->headers.add(key, value)))
                .get()
                .uri(uri)
                .responseSingle(this::resolveResponse);
    }

    default Mono<byte[]> postForm(URI uri, Map<String, String> formTextFields){
        return postForm(uri, Collections.emptyMap(), formTextFields, Collections.emptyMap());
    }

    default Mono<byte[]> postForm(URI uri, Map<String, String> formTextFields, Map<String, FileData>  formFiles){
        return postForm(uri, Collections.emptyMap(), formTextFields, formFiles);
    }

    default Mono<byte[]> postForm(URI uri, Consumer<HttpClientForm> formConsumer){
        return postForm(uri, Collections.emptyMap(), formConsumer);
    }

    default Mono<byte[]> postForm(URI uri, Map<String, String> headersMap, Map<String, String> formTextFields,
            Map<String, FileData> formFiles){
        return postForm(uri, headersMap, form->{
            form.charset(StandardCharsets.UTF_8);
            form.multipart(true);
            formTextFields.forEach(form::attr);
            formFiles.forEach((name, fileData)->form.file(name, fileData.getFileName(),
                    new ByteArrayInputStream(fileData.getBytes()), fileData.getContentType()));
        });
    }

    default Mono<byte[]> postForm(URI uri, Map<String, String> headersMap, Consumer<HttpClientForm> formConsumer){
        return httpClient()
                .headers(headers->headersMap.forEach((key, value)->headers.add(key, value)))
                .post()
                .uri(uri)
                .sendForm((req, form)->{
                    req.requestHeaders().remove(HttpHeaderNames.TRANSFER_ENCODING);
                    formConsumer.accept(form);
                })
                .responseSingle(this::resolveResponse);
    }

    default Mono<byte[]> resolveResponse(HttpClientResponse response, ByteBufMono content){
        return Mono.just(response)
                .flatMap(r->content.asByteArray()
                        .doOnNext(bytes->LOGGER.debug("Remote service response: {}", new String(bytes)))
                        .doOnNext(bytes->{
                            if(r.status().code() >= 400) {
                                throw new IllegalStateException("Remote service responsed with bad status "+response.status());
                            }
                        }));
    }

}
