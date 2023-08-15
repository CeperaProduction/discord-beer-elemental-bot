package me.cepera.discord.bot.beerelemental.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import me.cepera.discord.bot.beerelemental.model.Kingdom;
import me.cepera.discord.bot.beerelemental.repository.KingdomRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.db.SQLiteDatabase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SQLiteKingdomRepository extends SQLiteRepository implements KingdomRepository{

    public SQLiteKingdomRepository(SQLiteDatabase database) {
        super(database);
        prepareTables();
    }

    private void prepareTables() {
        connect(c->c.createStatement().execute("CREATE TABLE IF NOT EXISTS kingdoms ("
                    + "id integer PRIMARY KEY, "
                    + "guildId integer NOT NULL, "
                    + "roleId integer NOT NULL, "
                    + "name text NOT NULL, "
                    + "wolf_max_penalty integer NOT NULL)"));
    }

    @Override
    public Flux<Kingdom> getKingdoms(long guildId) {
        return Flux.defer(()->Flux.fromIterable(connect(c->{
            PreparedStatement stm = c.prepareStatement("SELECT id, roleId, name, wolf_max_penalty FROM kingdoms WHERE guildId = ?");
            stm.setLong(1, guildId);
            ResultSet rs = stm.executeQuery();
            List<Kingdom> kingdoms = new LinkedList<>();
            while(rs.next()) {
                Kingdom kd = new Kingdom();
                kd.setGuildId(guildId);
                kd.setId(rs.getInt(1));
                kd.setRoleId(rs.getLong(2));
                kd.setName(rs.getString(3));
                kd.setWolfMaxPenalty(rs.getByte(4));
                kingdoms.add(kd);
            }
            return kingdoms;
        })));
    }

    @Override
    public Mono<Kingdom> getKingdom(int id) {
        return Mono.fromSupplier(()->connect(c->{
            PreparedStatement stm = c.prepareStatement("SELECT guildId, roleId, name, wolf_max_penalty FROM kingdoms WHERE id = ?");
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();
            if(rs.next()) {
                Kingdom kd = new Kingdom();
                kd.setId(id);
                kd.setGuildId(rs.getLong(1));
                kd.setRoleId(rs.getLong(2));
                kd.setName(rs.getString(3));
                kd.setWolfMaxPenalty(rs.getByte(4));
                return kd;
            }
            return null;
        }));
    }

    @Override
    public Mono<Kingdom> getKingdomByName(long guildId, String name) {
        return Mono.fromSupplier(()->connect(c->{
            PreparedStatement stm = c.prepareStatement("SELECT id, roleId, name, wolf_max_penalty FROM kingdoms WHERE guildId = ? and name = ? COLLATE NOCASE");
            stm.setLong(1, guildId);
            stm.setString(2, name);
            ResultSet rs = stm.executeQuery();
            if(rs.next()) {
                Kingdom kd = new Kingdom();
                kd.setGuildId(guildId);
                kd.setId(rs.getInt(1));
                kd.setRoleId(rs.getLong(2));
                kd.setName(rs.getString(3));
                kd.setWolfMaxPenalty(rs.getByte(4));
                return kd;
            }
            return null;
        }));
    }

    @Override
    public Mono<Kingdom> getKingdomByRole(long guildId, long roleId) {
        return getKingdomsByRoles(guildId, Arrays.asList(roleId))
                .singleOrEmpty();
    }

    @Override
    public Flux<Kingdom> getKingdomsByRoles(long guildId, List<Long> roleIds) {
        if(roleIds.isEmpty()) {
            return Flux.empty();
        }
        return Flux.defer(()->Flux.fromIterable(connect(c->{
            String inCondition = "("+String.join(", ", roleIds.stream().map(id->id.toString()).collect(Collectors.toList()))+")";

            PreparedStatement stm = c.prepareStatement("SELECT id, name, roleId, wolf_max_penalty FROM kingdoms WHERE guildId = ? AND roleId IN "+inCondition);
            stm.setLong(1, guildId);
            ResultSet rs = stm.executeQuery();
            List<Kingdom> kingdoms = new LinkedList<>();
            while(rs.next()) {
                Kingdom kd = new Kingdom();
                kd.setGuildId(guildId);
                kd.setId(rs.getInt(1));
                kd.setName(rs.getString(2));
                kd.setRoleId(rs.getLong(3));
                kd.setWolfMaxPenalty(rs.getByte(4));
                kingdoms.add(kd);
            }
            return kingdoms;
        })));
    }

    @Override
    public Mono<Kingdom> saveKingdom(Kingdom kingdom) {
        return Mono.fromSupplier(()->connect(c->{

            if(kingdom.getId() != null) {
                PreparedStatement stm = c.prepareStatement("UPDATE kingdoms SET roleId = ?, name = ?, wolf_max_penalty = ? WHERE id = ?");
                stm.setLong(1, kingdom.getRoleId());
                stm.setString(2, kingdom.getName());
                stm.setInt(3, kingdom.getWolfMaxPenalty());
                stm.setInt(4, kingdom.getId());

                stm.executeUpdate();

            }else {
                PreparedStatement stm = c.prepareStatement("INSERT INTO kingdoms (guildId, roleId, name, wolf_max_penalty) VALUES (?, ?, ?, ?)");
                stm.setLong(1, kingdom.getGuildId());
                stm.setLong(2, kingdom.getRoleId());
                stm.setString(3, kingdom.getName());
                stm.setInt(4, kingdom.getWolfMaxPenalty());

                stm.executeUpdate();
                ResultSet rs = c.createStatement().executeQuery("SELECT last_insert_rowid()");
                if(rs.next()) {
                    int id = rs.getInt(1);
                    kingdom.setId(id);
                }
            }

            return kingdom;

        }));
    }

}
