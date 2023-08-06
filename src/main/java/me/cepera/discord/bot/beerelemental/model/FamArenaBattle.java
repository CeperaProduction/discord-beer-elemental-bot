package me.cepera.discord.bot.beerelemental.model;

import java.util.Objects;

public class FamArenaBattle {

    private int id;

    private long guildId;

    private String battler;

    private String opponent;

    private boolean asiat;

    private boolean win;

    private long timestamp;

    private String image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public String getBattler() {
        return battler;
    }

    public void setBattler(String battler) {
        this.battler = battler;
    }

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public boolean isAsiat() {
        return asiat;
    }

    public void setAsiat(boolean asiat) {
        this.asiat = asiat;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int hashCode() {
        return Objects.hash(asiat, battler, guildId, id, image, opponent, timestamp, win);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FamArenaBattle other = (FamArenaBattle) obj;
        return asiat == other.asiat && Objects.equals(battler, other.battler) && guildId == other.guildId
                && id == other.id && Objects.equals(image, other.image) && Objects.equals(opponent, other.opponent)
                && timestamp == other.timestamp && win == other.win;
    }

    @Override
    public String toString() {
        return "FamArenaBattle [id=" + id + ", guildId=" + guildId + ", battler=" + battler + ", opponent=" + opponent
                + ", asiat=" + asiat + ", win=" + win + ", timestamp=" + timestamp + ", image=" + image + "]";
    }

}
