package me.cepera.discord.bot.beerelemental.config;

import java.util.Objects;

public class OCRConfig {

    private String key;

    private OCRNickSettings nickSettings = new OCRNickSettings();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public OCRNickSettings getNickSettings() {
        return nickSettings;
    }

    public void setNickSettings(OCRNickSettings nickSettings) {
        this.nickSettings = nickSettings;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, nickSettings);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OCRConfig other = (OCRConfig) obj;
        return Objects.equals(key, other.key) && Objects.equals(nickSettings, other.nickSettings);
    }

    @Override
    public String toString() {
        return "OCRConfig [key=" + key + ", nickSettings=" + nickSettings + "]";
    }

}
