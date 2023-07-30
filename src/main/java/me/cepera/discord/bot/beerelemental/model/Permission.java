package me.cepera.discord.bot.beerelemental.model;

import java.util.Arrays;

public enum Permission {

    START_AUCTION,
    USE_IMAGE_TO_TEXT,
    MANAGE_KINGDOM,
    MANAGE_KINGDOM_MEMBERS,
    SHOW_KINGDOM_AND_MEMBERS_DATA;

    public String getValue() {
        return name().toLowerCase();
    }

    public static Permission fromValue(String value) {
        String search = value.replace(' ', '_').toLowerCase();
        return Arrays.stream(values())
                .filter(perm->perm.getValue().equals(search))
                .findAny()
                .orElse(null);

    }

}
