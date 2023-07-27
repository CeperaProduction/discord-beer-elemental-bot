package me.cepera.discord.bot.beerelemental.dto.ocr;

import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class OCRResultDto {

    private String parsedText;

    private String errorMessage;

    private String errorDetails;

    public String getParsedText() {
        return parsedText;
    }

    public void setParsedText(String parsedText) {
        this.parsedText = parsedText;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    @Override
    public String toString() {
        return "OCRResultDto [parsedText=" + parsedText + ", errorMessage=" + errorMessage + ", errorDetails="
                + errorDetails + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorDetails, errorMessage, parsedText);
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
        return Objects.equals(errorDetails, other.errorDetails) && Objects.equals(errorMessage, other.errorMessage)
                && Objects.equals(parsedText, other.parsedText);
    }

}
