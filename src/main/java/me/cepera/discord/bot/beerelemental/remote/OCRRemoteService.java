package me.cepera.discord.bot.beerelemental.remote;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import me.cepera.discord.bot.beerelemental.config.OCRConfig;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class OCRRemoteService implements RemoteService {

    private final URI image2textURI = URI.create("https://api.ocr.space/parse/image");

    private final OCRConfig ocrConfig;

    private HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(90));

    public HttpClient httpClient() {
        return httpClient;
    }

    @Inject
    public OCRRemoteService(OCRConfig ocrConfig) {
        this.ocrConfig = ocrConfig;
    }

    public Mono<byte[]> image2text(String fileName, String fileContentType, byte[] fileContent, Map<String, Object> params){
        Map<String, String> headers = new HashMap<>();
        headers.put("apikey", ocrConfig.getKey());
        Map<String, String> preparedFormFields = new HashMap<>();
        params.forEach((k,v)->preparedFormFields.put(k, v.toString()));
        Map<String, FileData> files = new HashMap<>();
        files.put("file", new FileData(fileName, fileContentType, fileContent));
        return postForm(image2textURI, headers, preparedFormFields, files);
    }

    public Mono<byte[]> image2text(String imageUrl, Map<String, Object> params){
        Map<String, String> headers = new HashMap<>();
        headers.put("apikey", ocrConfig.getKey());
        Map<String, String> preparedFormFields = new HashMap<>();
        params.forEach((k,v)->preparedFormFields.put(k, v.toString()));
        preparedFormFields.put("url", imageUrl);
        return postForm(image2textURI, headers, preparedFormFields, Collections.emptyMap());
    }

}
