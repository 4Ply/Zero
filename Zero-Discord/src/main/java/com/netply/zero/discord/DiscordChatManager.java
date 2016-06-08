package com.netply.zero.discord;

import com.netply.core.logging.Log;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.EventDispatcher;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.logging.Logger;

public class DiscordChatManager {
    private static final Logger log = Log.getLogger();
    private static DiscordChatManager instance;
    private final IDiscordClient discordClient;
    private boolean initCalled = false;


    private DiscordChatManager(IDiscordClient discordClient) {
        this.discordClient = discordClient;
    }

    public static DiscordChatManager getInstance() throws DiscordException {
        if (instance == null) {
            instance = new DiscordChatManager(getClient(Credentials.DISCORD_TOKEN, true));
        }
        return instance;
    }

    public static IDiscordClient getClient(String token, boolean login) throws DiscordException {
        ClientBuilder clientBuilder = new ClientBuilder();
        clientBuilder.withToken(token);
        if (login) {
            return clientBuilder.login(); // Creates the client instance and logs the client in
        } else {
            return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
        }
    }

    public void init() {
        if (!initCalled) {
            initCalled = true;
            EventDispatcher dispatcher = discordClient.getDispatcher();
            dispatcher.registerListener(new DiscordMessageReceivedEventListener());
        }
    }

    public void sendMessage(String uuid, String message) {
        IPrivateChannel pmChannel;
        try {
            pmChannel = discordClient.getOrCreatePMChannel(discordClient.getUserByID(uuid));
            pmChannel.sendMessage(message);
        } catch (DiscordException | HTTP429Exception | MissingPermissionsException e) {
            log.severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
