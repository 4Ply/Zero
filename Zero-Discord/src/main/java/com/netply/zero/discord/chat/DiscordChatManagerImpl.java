package com.netply.zero.discord.chat;

import com.netply.core.logging.Log;
import com.netply.zero.discord.DiscordMessageReceivedEventListener;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.EventDispatcher;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

public class DiscordChatManagerImpl implements DiscordChatManager {
    private final IDiscordClient discordClient;


    public DiscordChatManagerImpl(String discordAPIKey) throws DiscordException {
        this.discordClient = getClient(discordAPIKey, true);
        EventDispatcher dispatcher = discordClient.getDispatcher();
        dispatcher.registerListener(new DiscordMessageReceivedEventListener());
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
        IPrivateChannel pmChannel;
        try {
            pmChannel = discordClient.getOrCreatePMChannel(discordClient.getUserByID(uuid));
            pmChannel.sendMessage(message);
        } catch (DiscordException | HTTP429Exception | MissingPermissionsException e) {
            Log.getLogger().severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
