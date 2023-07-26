package me.cepera.discord.bot.beerelemental.model;

public enum GuildLocale {

    ENG("en-US"),
    RU("ru");

    private final String languageTag;

    private GuildLocale(String languageTag) {
        this.languageTag = languageTag;
    }

    public String getLanguageTag() {
        return languageTag;
    }

    public int index() {
        return ordinal();
    }

    public static GuildLocale fromIndex(Integer index) {
        if(index == null || index < 0) {
            return ENG;
        }
        GuildLocale[] values = values();
        if(index >= values.length) {
            return ENG;
        }
        return values[index];
    }

}
