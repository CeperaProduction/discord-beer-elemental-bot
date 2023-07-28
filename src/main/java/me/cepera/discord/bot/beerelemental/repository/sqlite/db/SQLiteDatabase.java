package me.cepera.discord.bot.beerelemental.repository.sqlite.db;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.cepera.discord.bot.beerelemental.repository.sqlite.SQLiteActiveAuctionRepository;

public class SQLiteDatabase {

    private static final Logger LOGGER = LogManager.getLogger(SQLiteActiveAuctionRepository.class);

    private final String filePath;

    private final ReentrantLock lock = new ReentrantLock();

    public SQLiteDatabase(Path path) {
        this.filePath = path.toAbsolutePath().toString();
    }

    public <T> T connect(ConnectionHandler<T> connectionConsumer) {

        String url = "jdbc:sqlite:" + filePath;

        lock.lock();

        try (Connection conn = DriverManager.getConnection(url)) {
            return connectionConsumer.apply(conn);
        } catch (SQLException e) {
            LOGGER.error("Error while working with database", e);
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }


    }

    public static interface ConnectionHandler<T> {

        T apply(Connection conn) throws SQLException;

    }

}
