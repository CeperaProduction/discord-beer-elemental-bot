package me.cepera.discord.bot.beerelemental.model;

import java.util.Objects;

public class RolePermission {

    private Integer id;

    private long guildId;

    private long roleId;

    private Permission permission;

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

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, id, permission, roleId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RolePermission other = (RolePermission) obj;
        return guildId == other.guildId && id == other.id && permission == other.permission && roleId == other.roleId;
    }

    @Override
    public String toString() {
        return "RolePermission [id=" + id + ", guildId=" + guildId + ", roleId=" + roleId + ", permission=" + permission
                + "]";
    }

}
