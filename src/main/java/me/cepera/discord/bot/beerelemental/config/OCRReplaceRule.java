package me.cepera.discord.bot.beerelemental.config;

import java.util.Objects;

public class OCRReplaceRule {

    private String from;

    private String to;

    public OCRReplaceRule() {}

    public OCRReplaceRule(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OCRReplaceRule other = (OCRReplaceRule) obj;
        return Objects.equals(from, other.from) && Objects.equals(to, other.to);
    }

    @Override
    public String toString() {
        return "OCRReplaceRule [from=" + from + ", to=" + to + "]";
    }

}
