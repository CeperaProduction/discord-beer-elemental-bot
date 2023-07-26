package me.cepera.discord.bot.beerelemental.repository;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import me.cepera.discord.bot.beerelemental.model.ActiveAuction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SQLiteActiveRepository extends SQLiteRepository implements ActiveAuctionRepository{

    public SQLiteActiveRepository(Path path) {
        super(path);
        preapreTable();
    }

    private void preapreTable() {
        openConnection(c->c.createStatement().execute("CREATE TABLE IF NOT EXISTS auctions ("
                    + "id integer PRIMARY KEY, "
                    + "guildId integer NOT NULL, "
                    + "channelId integer NOT NULL, "
                    + "messageId integer NOT NULL, "
                    + "roleId integer NOT NULL, "
                    + "count integer NOT NULL, "
                    + "timestamp integer NOT NULL)"));
    }

    private void writeAuction(ActiveAuction auction) {
        openConnection(c->{
            PreparedStatement stm = c.prepareStatement("INSERT INTO auctions (guildId, channelId, messageId, roleId, count, timestamp) VALUES (?, ?, ?, ?, ?, ?)");
            stm.setLong(1, auction.getGuildId());
            stm.setLong(2, auction.getChannelId());
            stm.setLong(3, auction.getMessageId());
            stm.setLong(4, auction.getRoleId());
            stm.setLong(5, auction.getCount());
            stm.setLong(6, auction.getTimestamp());
            return stm.executeUpdate();
        });
    }

    private void removeAuction(ActiveAuction auction) {
        if(auction.getId() == null) {
            return;
        }

        openConnection(c->{
            PreparedStatement stm = c.prepareStatement("DELETE FROM auctions WHERE id = ?");
            stm.setLong(1, auction.getId());
            return stm.executeUpdate();
        });
    }

    private List<ActiveAuction> findEndedActiveAuctions(long now){
        return openConnection(c->{
            List<ActiveAuction> result = new ArrayList<ActiveAuction>();
            PreparedStatement stm = c.prepareStatement("SELECT id, guildId, channelId, messageId, "
                    + "roleId, count, timestamp FROM auctions WHERE timestamp < ?");
            stm.setLong(1, now);
            ResultSet rs = stm.executeQuery();
            while(rs.next()) {

                ActiveAuction auction = new ActiveAuction();
                auction.setId(rs.getLong(1));
                auction.setGuildId(rs.getLong(2));
                auction.setChannelId(rs.getLong(3));
                auction.setMessageId(rs.getLong(4));
                auction.setRoleId(rs.getLong(5));
                auction.setCount(rs.getInt(6));
                auction.setTimestamp(rs.getLong(7));

                result.add(auction);

            }
            return result;
        });
    }

    @Override
    public Mono<Void> saveAuction(ActiveAuction auction) {
        return Mono.fromRunnable(()->writeAuction(auction));
    }

    @Override
    public Flux<ActiveAuction> getEndedActiveAuctions() {
        return Flux.fromIterable(findEndedActiveAuctions(System.currentTimeMillis()));
    }

    @Override
    public Mono<Void> deleteAuction(ActiveAuction auction) {
        return Mono.fromRunnable(()->removeAuction(auction));
    }



}
