package me.cepera.discord.bot.beerelemental.utils;

public enum ImageFormat {
    PNG("image/png"),
    JPEG("image/jpeg");

    private final String mimeType;

    private ImageFormat(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

}
