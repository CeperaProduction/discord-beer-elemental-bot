package me.cepera.discord.bot.beerelemental.local.lang;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.util.internal.ThrowableUtil;

public class LanguageLocalService {

    private static final Logger LOGGER = LogManager.getLogger(LanguageLocalService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Map<String, String>> keyToLangToValue;

    @Inject
    public LanguageLocalService() {
        this.keyToLangToValue = loadKeyToLangToValueMapping();
    }

    private Map<String, Map<String, String>> loadKeyToLangToValueMapping() {
        Map<String, Map<String, String>> result = new HashMap<>();
        readLanguageKeys().forEach(lang->readLanguageFile(lang)
                .forEach((key, value)->result.computeIfAbsent(key, k->new HashMap<>()).put(lang, value)));
        return result;
    }

    private List<String> readLanguageKeys(){
        try(InputStream in = getClass().getClassLoader().getResourceAsStream("lang/languages.json")){
            LanguageListFileContent fileContent = objectMapper.readValue(in, LanguageListFileContent.class);
            return fileContent.getLanguages();
        }catch(Exception e) {
            LOGGER.error("Error on loading language list: {}", ThrowableUtil.stackTraceToString(e));
        }
        return Collections.emptyList();
    }

    private Map<String, String> readLanguageFile(String lang){
        try(InputStream in = getClass().getClassLoader().getResourceAsStream("lang/"+lang+".json")){
            LanguageFileContent fileContent = objectMapper.readValue(in, LanguageFileContent.class);
            return fileContent.getContent();
        }catch(Exception e) {
            LOGGER.error("Error on loading language '{}': {}", lang, ThrowableUtil.stackTraceToString(e));
        }
        return Collections.emptyMap();
    }

    @Nullable
    public Map<String, String> getLocalizations(String key, Map<String, String> placeholderValues){
        Map<String, String> originalValues = keyToLangToValue.get(key);
        if(originalValues == null) {
            LOGGER.warn("Can't find any localization for key '{}'", key);
            return Collections.emptyMap();
        }
        Map<String, String> preparedValues = new HashMap<>();
        originalValues.forEach((k, v)->{
            String value = v;
            for(Entry<String, String> replacement : placeholderValues.entrySet()) {
                value = value.replace("{"+replacement.getKey()+"}", replacement.getValue());
            }
            value = value.replace("\\n", "\n");
            value = value.replace("\\t", "\t");
            preparedValues.put(k, value);
        });
        return preparedValues;
    }

    @Nullable
    public Map<String, String> getLocalizations(String key, String... replacementPairs){
        Map<String, String> replacements = new HashMap<>();
        for(int i = 1; i < replacementPairs.length; i+=2) {
            replacements.put(replacementPairs[i-1], replacementPairs[i]);
        }
        return getLocalizations(key, replacements);
    }

}
