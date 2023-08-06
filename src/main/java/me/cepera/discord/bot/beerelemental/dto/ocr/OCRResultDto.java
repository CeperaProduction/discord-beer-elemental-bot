package me.cepera.discord.bot.beerelemental.dto.ocr;

import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class OCRResultDto {

    private OCRTextOverlay textOverlay;

    private String parsedText;

    public OCRTextOverlay getTextOverlay() {
        return textOverlay;
    }

    public void setTextOverlay(OCRTextOverlay textOverlay) {
        this.textOverlay = textOverlay;
    }

    public String getParsedText() {
        return parsedText;
    }

    public void setParsedText(String parsedText) {
        this.parsedText = parsedText;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parsedText, textOverlay);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OCRResultDto other = (OCRResultDto) obj;
        return Objects.equals(parsedText, other.parsedText) && Objects.equals(textOverlay, other.textOverlay);
    }

    @Override
    public String toString() {
        return "OCRResultDto [textOverlay=" + textOverlay + ", parsedText=" + parsedText + "]";
    }

}
