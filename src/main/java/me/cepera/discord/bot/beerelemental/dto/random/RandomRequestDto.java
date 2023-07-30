package me.cepera.discord.bot.beerelemental.dto.random;

import java.util.Objects;

public class RandomRequestDto {

    private String jsonrpc = "2.0";

    private String method;

    private RandomParams params;

    private int id;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public RandomParams getParams() {
        return params;
    }

    public void setParams(RandomParams params) {
        this.params = params;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "RandomRequestDto [jsonrpc=" + jsonrpc + ", method=" + method + ", params=" + params + ", id=" + id
                + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jsonrpc, method, params);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RandomRequestDto other = (RandomRequestDto) obj;
        return id == other.id && Objects.equals(jsonrpc, other.jsonrpc) && Objects.equals(method, other.method)
                && Objects.equals(params, other.params);
    }

}
