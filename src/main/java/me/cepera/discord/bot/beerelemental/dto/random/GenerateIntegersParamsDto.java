package me.cepera.discord.bot.beerelemental.dto.random;

import java.util.Objects;

public class GenerateIntegersParamsDto extends RandomParams{

    private int n;

    private int min;

    private int max;

    private Boolean replacement;

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public Boolean getReplacement() {
        return replacement;
    }

    public void setReplacement(Boolean replacement) {
        this.replacement = replacement;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(max, min, n, replacement);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        GenerateIntegersParamsDto other = (GenerateIntegersParamsDto) obj;
        return max == other.max && min == other.min && n == other.n && Objects.equals(replacement, other.replacement);
    }

    @Override
    public String toString() {
        return "GenerateIntegersParams [n=" + n + ", min=" + min + ", max=" + max + ", replacement=" + replacement
                + "]";
    }

}
