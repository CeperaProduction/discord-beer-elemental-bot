package me.cepera.discord.bot.beerelemental.repository.sqlite;

import me.cepera.discord.bot.beerelemental.repository.sqlite.db.SQLiteDatabase;
import me.cepera.discord.bot.beerelemental.repository.sqlite.db.SQLiteDatabase.ConnectionHandler;

public abstract class SQLiteRepository {

    private final SQLiteDatabase database;

    public SQLiteRepository(SQLiteDatabase database) {
        this.database = database;
    }

    protected <T> T connect(ConnectionHandler<T> connectionConsumer) {
        return database.connect(connectionConsumer);
    }

}
