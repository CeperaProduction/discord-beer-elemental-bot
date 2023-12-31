package me.cepera.discord.bot.beerelemental.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import me.cepera.discord.bot.beerelemental.model.GuildLocale;
import me.cepera.discord.bot.beerelemental.repository.GuildLocaleRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.db.SQLiteDatabase;
import reactor.core.publisher.Mono;

public class SQLiteGuildLocaleRepository extends SQLiteRepository implements GuildLocaleRepository{

    public SQLiteGuildLocaleRepository(SQLiteDatabase database) {
        super(database);
        prepareTable();
    }

    private void prepareTable() {
        connect(c->c.createStatement().execute("CREATE TABLE IF NOT EXISTS guild_locales ("
                + "id integer PRIMARY KEY, "
                + "guildId integer NOT NULL, "
                + "localeId integer NOT NULL)"));
    }

    private void writeGuildLocale(long guildId, GuildLocale locale) {
        connect(c->{
            PreparedStatement stm = c.prepareStatement("INSERT INTO guild_locales (guildId, localeId) VALUES (?,?)");
            stm.setLong(1, guildId);
            stm.setInt(2, locale.index());
            return stm.executeUpdate();
        });
    }

    private GuildLocale readGuildLocale(long guildId) {
        Integer localeId = connect(c->{
            PreparedStatement stm = c.prepareStatement("SELECT localeId FROM guild_locales WHERE guildId = ?");
            stm.setLong(1, guildId);
            ResultSet res = stm.executeQuery();
            if(res.next()) {
                return res.getInt(1);
            }
            return null;
        });
        return GuildLocale.fromIndex(localeId);
    }

    @Override
    public Mono<Void> setGuildLocale(long guildId, GuildLocale locale) {
        return Mono.fromRunnable(()->writeGuildLocale(guildId, locale));
    }

    @Override
    public Mono<GuildLocale> getGuildLocale(long guildId) {
        return Mono.fromSupplier(()->readGuildLocale(guildId));
    }




}
