package com.netply.zero.discord.chat;

import com.netply.zero.discord.DiscordDisconnectEventListener;
import com.netply.zero.discord.DiscordMessageReceivedEventListener;
import com.netply.zero.discord.persistence.TrackedUserManager;
import com.sun.istack.Nullable;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.EventDispatcher;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.logging.Logger;

public class DiscordChatManagerImpl implements DiscordChatManager {
    private IDiscordClient discordClient;
    private String userID;


    public DiscordChatManagerImpl(String discordAPIKey, String botChanURL, TrackedUserManager trackedUserManager) throws DiscordException {
        this.discordClient = getClient(discordAPIKey, true);
        EventDispatcher dispatcher = discordClient.getDispatcher();
        dispatcher.registerListener(new DiscordMessageReceivedEventListener(botChanURL, trackedUserManager, this::getUserID));
        dispatcher.registerListener(new DiscordDisconnectEventListener(() -> System.exit(-1)));
    }

    @Nullable
    private String getUserID() {
        if (userID == null && discordClient.getOurUser() != null) {
            userID = discordClient.getOurUser().getID();
        }
        return userID;
    }

    private IDiscordClient getClient(String token, boolean login) throws DiscordException {
        ClientBuilder clientBuilder = new ClientBuilder();
        clientBuilder.withToken(token);
        if (login) {
            return clientBuilder.login(); // Creates the client instance and logs the client in
        } else {
            return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
        }
    }

    @Override
    public void sendMessage(String uuid, String message) {
        IChannel pmChannel;
        try {
            IUser userByID = discordClient.getUserByID(uuid);
            if (userByID != null) {
                pmChannel = discordClient.getOrCreatePMChannel(userByID);
            } else {
                pmChannel = discordClient.getChannelByID(uuid);
            }
            if (pmChannel != null) {
                pmChannel.sendMessage(message);
            }
        } catch (DiscordException | HTTP429Exception | MissingPermissionsException e) {
            Logger.getGlobal().severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
