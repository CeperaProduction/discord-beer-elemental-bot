package me.cepera.discord.bot.beerelemental.model;

import java.util.Objects;

public class KingdomMember {

    private Integer id;

    private String name;

    private int kingdomId;

    private Long discordUserId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKingdomId() {
        return kingdomId;
    }

    public void setKingdomId(int kingdomId) {
        this.kingdomId = kingdomId;
    }

    public Long getDiscordUserId() {
        return discordUserId;
    }

    public void setDiscordUserId(Long discordUserId) {
        this.discordUserId = discordUserId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(discordUserId, id, kingdomId, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KingdomMember other = (KingdomMember) obj;
        return Objects.equals(discordUserId, other.discordUserId) && id == other.id && kingdomId == other.kingdomId
                && Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return "KingdomMember [id=" + id + ", name=" + name + ", kingdomId=" + kingdomId + ", discordUserId="
                + discordUserId + "]";
    }

}
