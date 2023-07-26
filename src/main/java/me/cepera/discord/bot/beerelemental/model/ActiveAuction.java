package me.cepera.discord.bot.beerelemental.model;

import java.util.Objects;

public class ActiveAuction {

    private Long id;

    private long guildId;

    private long channelId;

    private long roleId;

    private long messageId;

    private int count;

    private long timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, count, guildId, messageId, roleId, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ActiveAuction other = (ActiveAuction) obj;
        return channelId == other.channelId && count == other.count && guildId == other.guildId
                && messageId == other.messageId && roleId == other.roleId && timestamp == other.timestamp;
    }

    @Override
    public String toString() {
        return "ActiveAuction [id=" + id + ", guildId=" + guildId + ", channelId=" + channelId + ", roleId=" + roleId
                + ", messageId=" + messageId + ", count=" + count + ", timestamp=" + timestamp + "]";
    }

}
