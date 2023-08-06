package me.cepera.discord.bot.beerelemental.local;

import java.util.Objects;

public class WordPosition {

    private final String word;

    private final int x;

    private final int y;

    private final int width;

    private final int height;

    public WordPosition(String word, int x, int y, int width, int height) {
        this.word = word;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public String getWord() {
        return word;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, width, word, x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WordPosition other = (WordPosition) obj;
        return height == other.height && width == other.width && Objects.equals(word, other.word) && x == other.x
                && y == other.y;
    }

    @Override
    public String toString() {
        return "WordPosition [word=" + word + ", x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }


}
