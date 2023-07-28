package me.cepera.discord.bot.beerelemental.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import me.cepera.discord.bot.beerelemental.model.Kingdom;
import me.cepera.discord.bot.beerelemental.model.KingdomMember;
import me.cepera.discord.bot.beerelemental.repository.KingdomMemberRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.db.SQLiteDatabase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SQLiteKingdomMemberRepository extends SQLiteRepository implements KingdomMemberRepository {

    public SQLiteKingdomMemberRepository(SQLiteDatabase database) {
        super(database);
        prepareTables();
    }

    private void prepareTables() {
        connect(c->c.createStatement().execute("CREATE TABLE IF NOT EXISTS kingdom_members ("
                    + "id integer PRIMARY KEY, "
                    + "discordUserId integer, "
                    + "kingdomId integer NOT NULL, "
                    + "name text NOT NULL)"));
    }

    @Override
    public Flux<KingdomMember> getMembers(Kingdom kingdom) {
        if(kingdom.getId() == null) {
            return Flux.empty();
        }
        return Flux.defer(()->Flux.fromIterable(connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT id, discordUserId, name FROM kingdom_members WHERE kingdomId = ?");
            stm.setInt(1, kingdom.getId());

            List<KingdomMember> members = new ArrayList<>();

            ResultSet rs = stm.executeQuery();
            while(rs.next()) {

                KingdomMember member = new KingdomMember();
                member.setId(rs.getInt(1));
                member.setDiscordUserId(rs.getLong(2));
                if(rs.wasNull() || member.getDiscordUserId() == 0) {
                    member.setDiscordUserId(null);
                }
                member.setName(rs.getString(3));

                members.add(member);

            }

            return members;

        })));
    }

    @Override
    public Flux<KingdomMember> findMembersByUser(long guildId, long userId) {
        return Flux.defer(()->Flux.fromIterable(connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT m.id as id, k.id as kingdomId, m.name as name FROM kingdom_members as m "
                    + "LEFT JOIN kingdoms as k ON k.id = m.kingdomId WHERE k.guildId = ? AND m.discordUserId = ?");
            stm.setLong(1, guildId);
            stm.setLong(2, userId);

            List<KingdomMember> members = new ArrayList<>();

            ResultSet rs = stm.executeQuery();
            while(rs.next()) {

                KingdomMember member = new KingdomMember();
                member.setId(rs.getInt(1));
                member.setDiscordUserId(userId);
                member.setKingdomId(rs.getInt(2));
                member.setName(rs.getString(3));

                members.add(member);

            }

            return members;

        })));
    }

    @Override
    public Flux<KingdomMember> findMembersByUser(Kingdom kingdom, long userId) {
        if(kingdom.getId() == null) {
            return Flux.empty();
        }
        return Flux.defer(()->Flux.fromIterable(connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT id, name FROM kingdom_members WHERE kingdomId = ? AND discordUserId = ?");
            stm.setInt(1, kingdom.getId());
            stm.setLong(2, userId);

            List<KingdomMember> members = new ArrayList<>();

            ResultSet rs = stm.executeQuery();
            while(rs.next()) {

                KingdomMember member = new KingdomMember();
                member.setId(rs.getInt(1));
                member.setName(rs.getString(2));
                member.setDiscordUserId(userId);
                member.setKingdomId(kingdom.getId());

                members.add(member);

            }

            return members;

        })));
    }

    @Override
    public Flux<KingdomMember> findMembersByNickname(long guildId, String nickname) {
        return Flux.defer(()->Flux.fromIterable(connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT m.id as id, k.id as kingdomId, m.name as name, "
                    + "m.discordUserId as discordUserId FROM kingdom_members as m "
                    + "LEFT JOIN kingdoms as k ON k.id = m.kingdomId WHERE k.guildId = ? AND m.name = ? COLLATE NOCASE");
            stm.setLong(1, guildId);
            stm.setString(2, nickname);

            List<KingdomMember> members = new ArrayList<>();

            ResultSet rs = stm.executeQuery();
            while(rs.next()) {

                KingdomMember member = new KingdomMember();
                member.setId(rs.getInt(1));
                member.setKingdomId(rs.getInt(2));
                member.setName(rs.getString(3));
                member.setDiscordUserId(rs.getLong(4));
                if(rs.wasNull() || member.getDiscordUserId() == 0) {
                    member.setDiscordUserId(null);
                }

                members.add(member);

            }

            return members;

        })));
    }

    @Override
    public Mono<KingdomMember> findMemberByNickname(Kingdom kingdom, String nickname) {
        if(kingdom.getId() == null) {
            return Mono.empty();
        }
        return Mono.fromSupplier(()->connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT id, discordUserId, name FROM kingdom_members WHERE kingdomId = ? AND name = ? COLLATE NOCASE");
            stm.setInt(1, kingdom.getId());
            stm.setString(2, nickname);

            ResultSet rs = stm.executeQuery();
            if(rs.next()) {

                KingdomMember member = new KingdomMember();
                member.setId(rs.getInt(1));
                member.setDiscordUserId(rs.getLong(2));
                if(rs.wasNull() || member.getDiscordUserId() == 0) {
                    member.setDiscordUserId(null);
                }
                member.setName(rs.getString(3));
                member.setKingdomId(kingdom.getId());

                return member;

            }

            return null;

        }));
    }

    @Override
    public Mono<KingdomMember> saveMember(KingdomMember member) {

        return Mono.fromSupplier(()->connect(c->{

            if(member.getId() == null) {

                PreparedStatement stm = c.prepareStatement("INSERT INTO kingdom_members (discordUserId, kingdomId, name) VALUES (?,?,?)");
                if(member.getDiscordUserId() != null) {
                    stm.setLong(1, member.getDiscordUserId());
                }else {
                    stm.setNull(1, Types.INTEGER);
                }
                stm.setInt(2, member.getKingdomId());
                stm.setString(3, member.getName());

                stm.executeUpdate();
                ResultSet rs = c.createStatement().executeQuery("SELECT last_insert_rowid()");

                if(rs.next()) {
                    member.setId(rs.getInt(1));
                }

            }else {

                PreparedStatement stm = c.prepareStatement("UPDATE kingdom_members SET discordUserId = ?, name = ? WHERE id = ?");
                if(member.getDiscordUserId() != null) {
                    stm.setLong(1, member.getDiscordUserId());
                }else {
                    stm.setNull(1, Types.INTEGER);
                }
                stm.setString(2, member.getName());
                stm.setInt(3, member.getId());

                stm.executeUpdate();

            }

            return member;
        }));
    }

    @Override
    public Mono<Void> deleteMember(KingdomMember member) {
        if(member.getId() == null) {
            return Mono.empty();
        }
        return Mono.fromRunnable(()->connect(c->{

            PreparedStatement stm = c.prepareStatement("DELETE FROM kingdom_members WHERE id = ?");
            stm.setInt(1, member.getId());
            stm.executeUpdate();

            return null;
        }));
    }

}
