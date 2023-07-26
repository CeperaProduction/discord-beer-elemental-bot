package me.cepera.discord.bot.beerelemental.repository;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SQLiteRepository {

    private static final Logger LOGGER = LogManager.getLogger(SQLiteActiveRepository.class);

    private final String filePath;

    private final ReentrantLock lock = new ReentrantLock();

    public SQLiteRepository(Path path) {
        this.filePath = path.toAbsolutePath().toString();
    }

    protected <T> T openConnection(ConnectionHandler<T> connectionConsumer) {

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

    protected static interface ConnectionHandler<T> {

        T apply(Connection conn) throws SQLException;

    }

}
