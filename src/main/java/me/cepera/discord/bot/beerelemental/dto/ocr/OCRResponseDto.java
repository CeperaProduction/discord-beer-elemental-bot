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

    @Override
    public int hashCode() {
        return Objects.hash(OCRExitCode, isErroredOnProcessing, parsedResults, processingTimeInMilliseconds);
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
        return Objects.equals(OCRExitCode, other.OCRExitCode)
                && Objects.equals(isErroredOnProcessing, other.isErroredOnProcessing)
                && Objects.equals(parsedResults, other.parsedResults)
                && Objects.equals(processingTimeInMilliseconds, other.processingTimeInMilliseconds);
    }

    @Override
    public String toString() {
        return "OCRResponseDto [parsedResults=" + parsedResults + ", processingTimeInMilliseconds="
                + processingTimeInMilliseconds + ", isErroredOnProcessing=" + isErroredOnProcessing + ", OCRExitCode="
                + OCRExitCode + "]";
    }

}
