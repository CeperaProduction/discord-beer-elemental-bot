package me.cepera.discord.bot.beerelemental.dto.random;

import java.util.Objects;

public class GenerateIntegersResultDto implements RandomResult{

    private RandomIntegerDto random;

    private Long bitsUsed;

    private Long bitsLeft;

    private Long requestsLeft;

    private Long advisoryDelay;

    public RandomIntegerDto getRandom() {
        return random;
    }

    public void setRandom(RandomIntegerDto random) {
        this.random = random;
    }

    public Long getBitsUsed() {
        return bitsUsed;
    }

    public void setBitsUsed(Long bitsUsed) {
        this.bitsUsed = bitsUsed;
    }

    public Long getBitsLeft() {
        return bitsLeft;
    }

    public void setBitsLeft(Long bitsLeft) {
        this.bitsLeft = bitsLeft;
    }

    public Long getRequestsLeft() {
        return requestsLeft;
    }

    public void setRequestsLeft(Long requestsLeft) {
        this.requestsLeft = requestsLeft;
    }

    public Long getAdvisoryDelay() {
        return advisoryDelay;
    }

    public void setAdvisoryDelay(Long advisoryDelay) {
        this.advisoryDelay = advisoryDelay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(advisoryDelay, bitsLeft, bitsUsed, random, requestsLeft);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GenerateIntegersResultDto other = (GenerateIntegersResultDto) obj;
        return Objects.equals(advisoryDelay, other.advisoryDelay) && Objects.equals(bitsLeft, other.bitsLeft)
                && Objects.equals(bitsUsed, other.bitsUsed) && Objects.equals(random, other.random)
                && Objects.equals(requestsLeft, other.requestsLeft);
    }

    @Override
    public String toString() {
        return "GenerateIntegersResultDto [random=" + random + ", bitsUsed=" + bitsUsed + ", bitsLeft=" + bitsLeft
                + ", requestsLeft=" + requestsLeft + ", advisoryDelay=" + advisoryDelay + "]";
    }

}
