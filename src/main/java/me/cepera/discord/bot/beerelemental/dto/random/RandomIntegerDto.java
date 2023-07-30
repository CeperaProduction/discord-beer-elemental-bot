package me.cepera.discord.bot.beerelemental.dto.random;

import java.util.List;
import java.util.Objects;

public class RandomIntegerDto {

    private List<Integer> data;

    private String completionTime;

    public List<Integer> getData() {
        return data;
    }

    public void setData(List<Integer> data) {
        this.data = data;
    }

    public String getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(String completionTime) {
        this.completionTime = completionTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(completionTime, data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RandomIntegerDto other = (RandomIntegerDto) obj;
        return Objects.equals(completionTime, other.completionTime) && Objects.equals(data, other.data);
    }

    @Override
    public String toString() {
        return "RandomIntegerDto [data=" + data + ", completionTime=" + completionTime + "]";
    }

}
