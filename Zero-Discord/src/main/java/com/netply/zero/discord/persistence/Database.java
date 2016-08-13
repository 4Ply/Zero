package com.netply.zero.discord.persistence;

import com.netply.botchan.web.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Database implements DiscordChatDatabase {
    private final static Logger LOGGER = Logger.getLogger(Database.class.getName());
    private Connection connection;
    private String mysqlIp;
    private int mysqlPort;
    private String mysqlDb;
    private final String mysqlUser;
    private final String mysqlPassword;


    public Database(String mysqlIp, int mysqlPort, String mysqlDb, String mysqlUser, String mysqlPassword) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
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
    public int addMessage(String targetUUID, String message) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO discord_messages (sender, target, message) VALUES ('', ?, ?)")) {
            preparedStatement.setString(1, targetUUID);
            preparedStatement.setString(2, message);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Database.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Message> getUnprocessedMessages() {
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM discord_messages WHERE processed = 0")) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                messages.add(new Message(resultSet.getString("id"), resultSet.getString("message"), resultSet.getString("target")));
            }
            return messages;
        } catch (SQLException e) {
            Database.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public int processMessage(long id) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE discord_messages SET processed = 1 WHERE id = ? AND processed = 0")) {
            preparedStatement.setLong(1, id);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Database.LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}
