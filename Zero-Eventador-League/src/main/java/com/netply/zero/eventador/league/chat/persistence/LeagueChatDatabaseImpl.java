package com.netply.zero.eventador.league.chat.persistence;

import java.sql.*;
import java.util.logging.Logger;

public class LeagueChatDatabaseImpl implements LeagueChatDatabase {
    private final static Logger LOGGER = Logger.getLogger(LeagueChatDatabaseImpl.class.getName());
    private Connection connection;
    private String mysqlIp;
    private int mysqlPort;
    private String mysqlDb;
    private final String mysqlUser;
    private final String mysqlPassword;


    public LeagueChatDatabaseImpl(String mysqlIp, int mysqlPort, String mysqlDb, String mysqlUser, String mysqlPassword) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        this.mysqlIp = mysqlIp;
        this.mysqlPort = mysqlPort;
        this.mysqlDb = mysqlDb;
        this.mysqlUser = mysqlUser;
        this.mysqlPassword = mysqlPassword;
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        connection = createConnection();
    }

    private Connection createConnection() throws SQLException {
        return createConnectionForCredentials(mysqlIp, mysqlPort, mysqlDb, mysqlUser, mysqlPassword);
    }

    private Connection createConnectionForCredentials(String mysqlIp, int mysqlPort, String mysqlDb, String mysqlUser, String mysqlPassword) throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + mysqlIp + ":" + mysqlPort + "/" + mysqlDb, mysqlUser, mysqlPassword);
    }

    private Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = createConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    @Override
    public int updateGameState(String summonerName, long gameID) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO league_games (summoner_name, game_id, timestamp) VALUES (?, ?, NOW());")) {
            preparedStatement.setString(1, summonerName);
            preparedStatement.setLong(2, gameID);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LeagueChatDatabaseImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long getCurrentGameId(String summonerName) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT game_id FROM league_games WHERE summoner_name = ? ORDER BY timestamp DESC LIMIT 1")) {
            preparedStatement.setString(1, summonerName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("game_id");
            }
        } catch (SQLException e) {
            LeagueChatDatabaseImpl.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
}
