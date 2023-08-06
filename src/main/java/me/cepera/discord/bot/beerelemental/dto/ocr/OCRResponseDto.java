package me.cepera.discord.bot.beerelemental.dto.ocr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class OCRResponseDto {

    private List<OCRResultDto> parsedResults = new ArrayList<>();

    private Long processingTimeInMilliseconds;

    private Boolean isErroredOnProcessing;

    private Integer OCRExitCode;

    private List<String> errorMessage;

    private String errorDetails;

    public List<OCRResultDto> getParsedResults() {
        return parsedResults;
    }

    public void setParsedResults(List<OCRResultDto> parsedResults) {
        this.parsedResults = parsedResults;
    }

    public Long getProcessingTimeInMilliseconds() {
        return processingTimeInMilliseconds;
    }

    public void setProcessingTimeInMilliseconds(Long processingTimeInMilliseconds) {
        this.processingTimeInMilliseconds = processingTimeInMilliseconds;
    }

    public Boolean getIsErroredOnProcessing() {
        return isErroredOnProcessing;
    }

    public void setIsErroredOnProcessing(Boolean isErroredOnProcessing) {
        this.isErroredOnProcessing = isErroredOnProcessing;
    }

    public Integer getOCRExitCode() {
        return OCRExitCode;
    }

    public void setOCRExitCode(Integer oCRExitCode) {
        OCRExitCode = oCRExitCode;
    }

    public List<String> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(List<String> errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    @Override
    public int hashCode() {
        return Objects.hash(OCRExitCode, errorDetails, errorMessage, isErroredOnProcessing, parsedResults,
                processingTimeInMilliseconds);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OCRResponseDto other = (OCRResponseDto) obj;
        return Objects.equals(OCRExitCode, other.OCRExitCode) && Objects.equals(errorDetails, other.errorDetails)
                && Objects.equals(errorMessage, other.errorMessage)
                && Objects.equals(isErroredOnProcessing, other.isErroredOnProcessing)
                && Objects.equals(parsedResults, other.parsedResults)
                && Objects.equals(processingTimeInMilliseconds, other.processingTimeInMilliseconds);
    }

    @Override
    public String toString() {
        return "OCRResponseDto [parsedResults=" + parsedResults + ", processingTimeInMilliseconds="
                + processingTimeInMilliseconds + ", isErroredOnProcessing=" + isErroredOnProcessing + ", OCRExitCode="
                + OCRExitCode + ", errorMessage=" + errorMessage + ", errorDetails=" + errorDetails + "]";
    }

}
