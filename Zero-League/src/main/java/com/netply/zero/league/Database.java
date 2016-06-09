package com.netply.zero.league;

import com.netply.core.logging.Log;
import com.netply.core.running.ProcessRunner;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class Database {
    private final static Logger LOGGER = Log.getLogger();
    private static Database instance;
    private final BlockingDeque<Consumer<Connection>> queue = new LinkedBlockingDeque<>();
    private final Connection conn;


    private Database() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection("jdbc:mysql://" + Credentials.MYSQL_IP + ":" + Credentials.MYSQL_PORT + "/" + Credentials.MYSQL_DB, Credentials.MYSQL_USER, Credentials.MYSQL_PASSWORD);

        new Thread() {
            @Override
            public void run() {
                while (ProcessRunner.run) {
                    consumeQueue();
                }
            }
        }.start();
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            try {
                instance = new Database();
            } catch (ClassNotFoundException | IllegalAccessException | SQLException | InstantiationException e) {
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
        }
        return instance;
    }

    private void consumeQueue() {
        try {
            Consumer<Connection> take = queue.take();
            take.accept(conn);
        } catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
    }

    public <R> void addToQueue(Function<Connection, R> function, Consumer<R> consumer) {
        queue.add(connection -> {
            R apply = function.apply(connection);
            if (consumer != null) {
                consumer.accept(apply);
            }
        });
    }

    public static ArrayList<String> getTrackedLoLSummoners(Connection connection, String fromJID) {
        ArrayList<String> summoners = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT summoner_name FROM tracked_summoners WHERE trackerJID LIKE ? GROUP BY summoner_name");
            statement.setString(1, fromJID);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                summoners.add(resultSet.getString("summoner_name"));
            }

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return summoners;
    }

    public static ArrayList<String> getAllTrackedLoLSummoners(Connection connection) {
        ArrayList<String> summoners = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT summoner_name FROM tracked_summoners GROUP BY summoner_name");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                summoners.add(resultSet.getString("summoner_name"));
            }

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return summoners;
    }

    public static boolean trackLoLPlayer(Connection connection, String summonerName, String trackerJID) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO tracked_summoners (id, summoner_name, trackerJID) VALUES (NULL, ?, ?)");
            statement.setString(1, summonerName);
            statement.setString(2, trackerJID);

            int i = statement.executeUpdate();
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean logCurrentGame(Connection connection, long gameID, String summonerName) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO tracked_games (id, game_id, summoner_name, processed) VALUES (NULL, ?, ?, 0)");
            statement.setLong(1, gameID);
            statement.setString(2, summonerName);

            int i = statement.executeUpdate();
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static HashMap<String, Long> getUnProcessedLoLGames(Connection connection) {
        HashMap<String, Long> list = new HashMap<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT summoner_name, game_id FROM tracked_games WHERE processed = 0");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                list.put(resultSet.getString("summoner_name"), resultSet.getLong("game_id"));
            }
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<String> getTrackersForSummoner(Connection connection, String summonerName) {
        ArrayList<String> trackers = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT trackerJID FROM tracked_summoners WHERE summoner_name LIKE ?");
            statement.setString(1, summonerName);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                trackers.add(resultSet.getString("trackerJID"));
            }
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return trackers;
    }
}
