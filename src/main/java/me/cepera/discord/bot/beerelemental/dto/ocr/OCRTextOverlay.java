package me.cepera.discord.bot.beerelemental.dto.ocr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class OCRTextOverlay {

    private List<OCRTextLine> lines = new ArrayList<>();

    private boolean hasOverlay;

    public List<OCRTextLine> getLines() {
        return lines;
    }

    public void setLines(List<OCRTextLine> lines) {
        this.lines = lines;
    }

    public boolean isHasOverlay() {
        return hasOverlay;
    }

    public void setHasOverlay(boolean hasOverlay) {
        this.hasOverlay = hasOverlay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasOverlay, lines);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OCRTextOverlay other = (OCRTextOverlay) obj;
        return hasOverlay == other.hasOverlay && Objects.equals(lines, other.lines);
    }

    @Override
    public String toString() {
        return "OCRTextOverlay [lines=" + lines + ", hasOverlay=" + hasOverlay + "]";
    }

}
