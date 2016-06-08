package com.netply.zero.league;

import com.netply.core.logging.Log;
import com.netply.core.running.ItemNotFoundException;
import com.netply.core.running.ProcessRunner;
import com.netply.zero.messaging.base.poco.*;
import sx.blah.discord.handle.obj.IMessage;

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

    public void addMangaFoxFeed(String mangaName, String name, String url, String timeStamp) {
        addToQueue((connection) -> Database.getInstance().postAddMangaFoxFeed(connection, new MangaFoxFeedItem(mangaName, name, url, timeStamp)), null);
    }

    public boolean postAddMangaFoxFeed(Connection connection, FeedItem feedItem) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT IGNORE INTO " + Credentials.MYSQL_DB + "fox_chapter_stage (id, manga_id, chapter_name, url, timestamp) VALUES (NULL, ?, ?, ?, ?)");

            preparedStatement.setInt(1, getMangaId(connection, feedItem.getMangaName().trim()));
            preparedStatement.setString(2, feedItem.getName().trim());
            preparedStatement.setString(3, feedItem.getUrl().trim());
            preparedStatement.setString(4, feedItem.getTimeStamp().trim());
            preparedStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            return false;
        } catch (ItemNotFoundException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static HashMap<String, ArrayList<FeedItem>> getUnProcessedFeeds(Connection connection) {
        HashMap<String, ArrayList<FeedItem>> map = new HashMap<>();
        try {

            ResultSet resultSet = connection.prepareStatement("SELECT * FROM " + Credentials.MYSQL_DB + "fox_chapter_stage WHERE processed = 0 ORDER BY timestamp DESC").executeQuery();
            HashMap<Integer, String> mangaIds = new HashMap<>();
            while (resultSet.next()) {
                try {
                    int id = resultSet.getInt(Credentials.MYSQL_DB + "_id");
                    String mangaName;
                    if (mangaIds.containsKey(id)) {
                        mangaName = mangaIds.get(id);
                    } else {
                        mangaName = getMangaName(connection, id);
                        mangaIds.put(id, mangaName);
                    }

                    if (!map.containsKey(mangaName)) {
                        map.put(mangaName, new ArrayList<>());
                    }
                    map.get(mangaName).add(new FeedItem(mangaName, resultSet.getString("chapter_name"), resultSet.getString("url"), resultSet.getString("timestamp")));
                } catch (Exception e) {
                    LOGGER.severe(e.getMessage());
                    e.printStackTrace();
                    resultSet.close();
                }
            }
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return map;
    }

    public static ArrayList<String> getWhatsAppJidListForMangaName(Connection connection, String mangaName) {
        ArrayList<String> strings = new ArrayList<>();
        try {

            ResultSet resultSet = connection.prepareStatement(String.format("SELECT * FROM " + Credentials.MYSQL_DB + "_jids WHERE manga_name LIKE '%s'", mangaName)).executeQuery();
            while (resultSet.next()) {
                strings.add(resultSet.getString("jid"));
            }

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return strings;
    }

    public static boolean addMangaFoxUrl(Connection connection, String mangaName, String url) {
        int mangaId;
        try {
            mangaId = getMangaId(connection, mangaName);
        } catch (ItemNotFoundException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            return false;
        }

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + Credentials.MYSQL_DB + "fox_urls (id, manga_id, url)" +
                    " SELECT NULL, ?, ? FROM `" + Credentials.MYSQL_DB + "fox_urls`" +
                    " WHERE NOT EXISTS(SELECT * FROM `" + Credentials.MYSQL_DB + "fox_urls` WHERE url = ?) LIMIT 1;");
            statement.setInt(1, mangaId);
            statement.setString(2, url);
            statement.setString(3, url);

            int i = statement.executeUpdate();

            return i >= 1;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static int getMangaId(Connection connection, String mangaName) throws ItemNotFoundException {
        try {
            ResultSet resultSet = connection.prepareStatement("SELECT id FROM " + Credentials.MYSQL_DB + "_items WHERE manga_name LIKE '" + mangaName + "'").executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        throw new ItemNotFoundException("Could not find " + Credentials.MYSQL_DB + " ID for manga name: " + mangaName);
    }

    private static String getMangaName(Connection connection, int id) throws ItemNotFoundException {
        try {
            ResultSet resultSet = connection.prepareStatement("SELECT " + Credentials.MYSQL_DB + "_name FROM manga_items WHERE id = '" + id + "'").executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(Credentials.MYSQL_DB + "_name");
            }
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        throw new ItemNotFoundException("Could not find " + Credentials.MYSQL_DB + " name for manga ID: " + id);
    }

    public static ArrayList<Feed> getMangaFoxFeeds(Connection connection) {
        ArrayList<Feed> feeds = new ArrayList<>();
        try {
            ResultSet resultSet = connection.prepareStatement("SELECT id, url FROM " + Credentials.MYSQL_DB + "fox_urls").executeQuery();

            HashMap<Integer, String> mangaIds = new HashMap<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String url = resultSet.getString("url");

                try {
                    String mangaName;
                    if (mangaIds.containsKey(id)) {
                        mangaName = mangaIds.get(id);
                    } else {
                        mangaName = getMangaName(connection, id);
                        mangaIds.put(id, mangaName);
                    }

                    feeds.add(new Feed(mangaName, url, FeedLocation.MANGA_FOX));
                } catch (ItemNotFoundException e) {
                    LOGGER.severe(e.getMessage());
                    e.printStackTrace();
                }
            }

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return feeds;
    }

    public static boolean logIncomingWhatsAppMessage(Connection connection, ReceivedWhatsAppMessage message) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO whatsapp_logs (id, message, jid, type) " +
                    "VALUES (NULL , ?, ?, ?)");
            preparedStatement.setString(1, message.getMessage());
            preparedStatement.setString(2, message.isGroupMessage() ? message.getAuthorJID() + "/" + message.getUUID() : message.getUUID());
            preparedStatement.setString(3, "RECEIVE");
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean logOutgoingWhatsAppMessage(Connection connection, SendWhatsAppMessage message) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO whatsapp_logs (id, message, jid, type) " +
                    "VALUES (NULL , ?, ?, ?)");
            preparedStatement.setString(1, message.getMessage());
            preparedStatement.setString(2, message.getUUID());
            preparedStatement.setString(3, "SEND");
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean logIncomingDiscordMessage(Connection connection, IMessage message) {
        return logDiscordMessage(connection, "RECEIVE", message.getContent(), message.getAuthor().getID(), message.getChannel().isPrivate());
    }

    public static boolean logOutgoingDiscordMessage(Connection connection, String uuid, String messageContent) {
        return logDiscordMessage(connection, "SEND", messageContent, uuid, true);
    }

    private static boolean logDiscordMessage(Connection connection, String type, String messageContent, String uuid, boolean isPrivate) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO discord_logs (id, message, author_id, direct_message, type, `timestamp`) VALUES (NULL , ?, ?, ?, ?, NOW())")) {
            preparedStatement.setString(1, messageContent);
            preparedStatement.setString(2, uuid);
            preparedStatement.setBoolean(3, isPrivate);
            preparedStatement.setString(4, type);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean clearUnProcessedFeeds(Connection connection) {
        try {
            connection.prepareStatement("UPDATE " + Credentials.MYSQL_DB + "fox_chapter_stage SET processed = 1 WHERE processed = 0").executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    public static boolean addManga(Connection connection, String mangaName) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + Credentials.MYSQL_DB + "_items (id, manga_name) " +
                    "VALUES (NULL , ?)");
            preparedStatement.setString(1, mangaName);
            int i = preparedStatement.executeUpdate();
            return i >= 1;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }
        return false;
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
