package me.cepera.discord.bot.beerelemental.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import me.cepera.discord.bot.beerelemental.model.FamArenaBattle;
import me.cepera.discord.bot.beerelemental.repository.FamArenaBattleRepository;
import me.cepera.discord.bot.beerelemental.repository.sqlite.db.SQLiteDatabase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class SQLiteFamArenaRepository extends SQLiteRepository implements FamArenaBattleRepository{

    public SQLiteFamArenaRepository(SQLiteDatabase database) {
        super(database);
        prepareTable();
    }

    private void prepareTable() {
        connect(c->c.createStatement().execute("CREATE TABLE IF NOT EXISTS fam_arena_battles ("
                + "id integer PRIMARY KEY, "
                + "guildId integer NOT NULL, "
                + "battler text, "
                + "opponent text NOT NULL, "
                + "opponentLower text NOT NULL, "
                + "asiat integer NOT NULL, "
                + "win integer NOT NULL, "
                + "timestamp integer NOT NULL, "
                + "image text NOT NULL)"));
    }

    private String prepareSearch(String searchStr) {
        return "%"+searchStr.replace("[", "_")
                .replace("]", "_")
                .replace("+", "_")
                .replace("*", "_")
                .replace("%", "_")
                .replace("!", "_") + "%";
    }

    @Override
    public Flux<String> findOpponentNicknames(long guildId) {
        return Flux.defer(()->Flux.fromIterable(connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT DISTINCT opponent, opponentLower FROM fam_arena_battles WHERE guildId = ?");
            stm.setLong(1, guildId);

            ResultSet rs = stm.executeQuery();

            Set<Tuple2<String, String>> opponents = new TreeSet<>((t1, t2)->t1.getT2().compareTo(t2.getT2()));

            while(rs.next()) {
                opponents.add(Tuples.of(rs.getString(1), rs.getString(2)));
            }

            return opponents.stream().map(Tuple2::getT1).collect(Collectors.toList());

        })));
    }

    @Override
    public Flux<String> findOpponentNicknames(long guildId, String search) {
        return Flux.defer(()->Flux.fromIterable(connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT DISTINCT opponent, opponentLower FROM fam_arena_battles WHERE guildId = ? "
                    + "AND opponentLower LIKE ?");
            stm.setLong(1, guildId);
            stm.setString(2, prepareSearch(search.toLowerCase()));

            ResultSet rs = stm.executeQuery();

            Set<Tuple2<String, String>> opponents = new TreeSet<>((t1, t2)->t1.getT2().compareTo(t2.getT2()));

            while(rs.next()) {
                opponents.add(Tuples.of(rs.getString(1), rs.getString(2)));
            }

            return opponents.stream().map(Tuple2::getT1).collect(Collectors.toList());

        })));
    }

    @Override
    public Flux<FamArenaBattle> findOpponentBattles(long guildId, String search, long minTimestamp, int offset,
            int count, Boolean winOnly) {
        return Flux.defer(()->Flux.fromIterable(connect(c->{

            PreparedStatement stm = c.prepareStatement("SELECT id, battler, opponent, asiat, win, timestamp, image "
                    + "FROM fam_arena_battles WHERE guildId = ? AND timestamp >= ? "
                    + "AND opponentLower LIKE ? "
                    + (winOnly == null ? "" : winOnly.booleanValue() ? "AND win = 1 " : "AND win = 0 ")
                    + "LIMIT "+count+" OFFSET "+offset);
            stm.setLong(1, guildId);
            stm.setLong(2, minTimestamp);
            stm.setString(3, prepareSearch(search.toLowerCase()));

            ResultSet rs = stm.executeQuery();

            List<FamArenaBattle> battles = new LinkedList<>();

            while(rs.next()) {

                FamArenaBattle battle = new FamArenaBattle();
                battle.setId(rs.getInt(1));
                battle.setGuildId(guildId);
                battle.setBattler(rs.getString(2));
                battle.setOpponent(rs.getString(3));
                battle.setAsiat(rs.getBoolean(4));
                battle.setWin(rs.getBoolean(5));
                battle.setTimestamp(rs.getLong(6));
                battle.setImage(rs.getString(7));

                battles.add(battle);

            }

            return battles;

        })));
    }

    @Override
    public Mono<FamArenaBattle> addBattle(FamArenaBattle battle) {
        return Mono.fromSupplier(()->connect(c->{

            PreparedStatement stm = c.prepareStatement("INSERT INTO fam_arena_battles "
                    + "(guildId, battler, opponent, opponentLower, asiat, win, timestamp, image) VALUES (?,?,?,?,?,?,?,?)");
            stm.setLong(1, battle.getGuildId());
            stm.setString(2, battle.getBattler());
            stm.setString(3, battle.getOpponent());
            stm.setString(4, battle.getOpponent().toLowerCase());
            stm.setInt(5, battle.isAsiat() ? 1 : 0);
            stm.setInt(6, battle.isWin() ? 1 : 0);
            stm.setLong(7, battle.getTimestamp());
            stm.setString(8, battle.getImage());

            stm.executeUpdate();

            ResultSet rs = c.createStatement().executeQuery("SELECT last_insert_rowid()");
            if(rs.next()) {
                battle.setId(rs.getInt(1));
            }

            return battle;
        }));
    }

}
