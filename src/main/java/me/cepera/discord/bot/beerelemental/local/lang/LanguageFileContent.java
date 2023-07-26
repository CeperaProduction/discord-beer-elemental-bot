package me.cepera.discord.bot.beerelemental.local.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LanguageFileContent {

    private Map<String, String> content = new HashMap<String, String>();

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LanguageFileContent other = (LanguageFileContent) obj;
        return Objects.equals(content, other.content);
    }

    @Override
    public String toString() {
        return "LanguageFileContent [content=" + content + "]";
    }

}
