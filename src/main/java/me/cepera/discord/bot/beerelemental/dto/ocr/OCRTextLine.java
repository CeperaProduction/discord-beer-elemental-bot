package me.cepera.discord.bot.beerelemental.dto.ocr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class OCRTextLine {

    private List<OCRTextWord> words = new ArrayList<>();

    private int maxHeight;

    private int minTop;

    public List<OCRTextWord> getWords() {
        return words;
    }

    public void setWords(List<OCRTextWord> words) {
        this.words = words;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMinTop() {
        return minTop;
    }

    public void setMinTop(int minTop) {
        this.minTop = minTop;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxHeight, minTop, words);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OCRTextLine other = (OCRTextLine) obj;
        return maxHeight == other.maxHeight && minTop == other.minTop && Objects.equals(words, other.words);
    }

    @Override
    public String toString() {
        return "OCRTextLine [words=" + words + ", maxHeight=" + maxHeight + ", minTop=" + minTop + "]";
    }

}
