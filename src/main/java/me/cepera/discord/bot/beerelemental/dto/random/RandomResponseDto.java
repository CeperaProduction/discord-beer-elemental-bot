package me.cepera.discord.bot.beerelemental.dto.random;

import java.util.Objects;

public class RandomResponseDto <T extends RandomResult>  {

    private String jsonrpc;

    private T result;

    private RandomErrorDto error;

    private int id;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public RandomErrorDto getError() {
        return error;
    }

    public void setError(RandomErrorDto error) {
        this.error = error;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, id, jsonrpc, result);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RandomResponseDto<?> other = (RandomResponseDto<?>) obj;
        return Objects.equals(error, other.error) && id == other.id && Objects.equals(jsonrpc, other.jsonrpc)
                && Objects.equals(result, other.result);
    }

    @Override
    public String toString() {
        return "RandomResponseDto [jsonrpc=" + jsonrpc + ", result=" + result + ", error=" + error + ", id=" + id + "]";
    }

}
