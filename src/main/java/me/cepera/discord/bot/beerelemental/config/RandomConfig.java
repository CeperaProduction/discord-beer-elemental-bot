package me.cepera.discord.bot.beerelemental.config;

import java.util.Objects;

public class RandomConfig {

    private boolean remote = true;

    private String key;

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, remote);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RandomConfig other = (RandomConfig) obj;
        return Objects.equals(key, other.key) && remote == other.remote;
    }

    @Override
    public String toString() {
        return "RandomConfig [remote=" + remote + ", key=" + key + "]";
    }

}
