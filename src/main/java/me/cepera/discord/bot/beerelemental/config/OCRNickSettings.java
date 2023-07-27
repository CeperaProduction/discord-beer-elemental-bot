package me.cepera.discord.bot.beerelemental.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OCRNickSettings {

    private List<String> ignore = new ArrayList<>();

    private List<OCRReplaceRule> replace = new ArrayList<>();

    public List<String> getIgnore() {
        return ignore;
    }

    public void setIgnore(List<String> ignore) {
        this.ignore = ignore;
    }

    public List<OCRReplaceRule> getReplace() {
        return replace;
    }

    public void setReplace(List<OCRReplaceRule> replace) {
        this.replace = replace;
    }

    @Override
    public String toString() {
        return "OCRNickSettings [ignore=" + ignore + ", replace=" + replace + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignore, replace);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OCRNickSettings other = (OCRNickSettings) obj;
        return Objects.equals(ignore, other.ignore) && Objects.equals(replace, other.replace);
    }

}
