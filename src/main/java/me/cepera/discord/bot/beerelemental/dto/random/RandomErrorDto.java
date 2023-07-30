package me.cepera.discord.bot.beerelemental.dto.random;

import java.util.Objects;

public class RandomErrorDto {

    private int code;

    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RandomErrorDto other = (RandomErrorDto) obj;
        return code == other.code && Objects.equals(message, other.message);
    }

    @Override
    public String toString() {
        return "RandomErrorDto [code=" + code + ", message=" + message + "]";
    }

}
