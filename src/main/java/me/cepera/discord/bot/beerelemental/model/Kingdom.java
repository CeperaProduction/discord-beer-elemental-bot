package me.cepera.discord.bot.beerelemental.model;

import java.util.Objects;

public class Kingdom {

    private Integer id;

    private long guildId;

    private String name;

    private long roleId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "Kingdom [id=" + id + ", guildId=" + guildId + ", name=" + name + ", roleId=" + roleId + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, id, name, roleId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Kingdom other = (Kingdom) obj;
        return guildId == other.guildId && id == other.id && Objects.equals(name, other.name) && roleId == other.roleId;
    }

}
