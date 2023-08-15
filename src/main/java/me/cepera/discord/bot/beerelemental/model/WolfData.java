package me.cepera.discord.bot.beerelemental.model;

import java.util.Objects;

public class WolfData {

    private byte wolfs;

    private byte penalty;

    private boolean received;

    public byte getWolfs() {
        return wolfs;
    }

    public void setWolfs(byte wolfs) {
        this.wolfs = wolfs;
    }

    public byte getPenalty() {
        return penalty;
    }

    public void setPenalty(byte penalty) {
        this.penalty = penalty;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    @Override
    public int hashCode() {
        return Objects.hash(penalty, received, wolfs);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WolfData other = (WolfData) obj;
        return penalty == other.penalty && received == other.received && wolfs == other.wolfs;
    }

    @Override
    public String toString() {
        return "WolfData [wolfs=" + wolfs + ", penalty=" + penalty + ", received=" + received + "]";
    }



}
