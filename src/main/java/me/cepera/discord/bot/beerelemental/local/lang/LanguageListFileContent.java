package me.cepera.discord.bot.beerelemental.local.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LanguageListFileContent {

    private List<String> languages = new ArrayList<String>();

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    @Override
    public int hashCode() {
        return Objects.hash(languages);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LanguageListFileContent other = (LanguageListFileContent) obj;
        return Objects.equals(languages, other.languages);
    }

    @Override
    public String toString() {
        return "LanguageListFileContent [languages=" + languages + "]";
    }

}
