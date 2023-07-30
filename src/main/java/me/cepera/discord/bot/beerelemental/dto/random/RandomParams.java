package me.cepera.discord.bot.beerelemental.dto.random;

import java.util.Objects;

public abstract class RandomParams {

    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RandomParams other = (RandomParams) obj;
        return Objects.equals(apiKey, other.apiKey);
    }

    @Override
    public String toString() {
        return "RandomParams [apiKey=" + apiKey + "]";
    }

}
