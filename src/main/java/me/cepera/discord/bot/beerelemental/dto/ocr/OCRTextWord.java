package me.cepera.discord.bot.beerelemental.dto.ocr;

import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class OCRTextWord {

    private String wordText;

    private int left;

    private int top;

    private int height;

    private int width;

    public String getWordText() {
        return wordText;
    }

    public void setWordText(String wordText) {
        this.wordText = wordText;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, left, top, width, wordText);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OCRTextWord other = (OCRTextWord) obj;
        return height == other.height && left == other.left && top == other.top && width == other.width
                && Objects.equals(wordText, other.wordText);
    }

    @Override
    public String toString() {
        return "OCRTextWord [wordText=" + wordText + ", left=" + left + ", top=" + top + ", height=" + height
                + ", width=" + width + "]";
    }

}
