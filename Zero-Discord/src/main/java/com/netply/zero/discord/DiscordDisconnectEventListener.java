package com.netply.zero.discord;

import com.netply.zero.discord.live.DiscordReconnectManager;
import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;

import java.util.logging.Logger;

public class DiscordDisconnectEventListener implements IListener<DiscordDisconnectedEvent> {
    private DiscordReconnectManager discordReconnectManager;


    public DiscordDisconnectEventListener(DiscordReconnectManager discordReconnectManager) {
        this.discordReconnectManager = discordReconnectManager;
    }

    @Override
    public void handle(DiscordDisconnectedEvent event) {
        Logger.getGlobal().info("Disconnected (" + event.getReason().name() + ")");
        if (event.getReason() == DiscordDisconnectedEvent.Reason.UNKNOWN ||
                event.getReason() == DiscordDisconnectedEvent.Reason.TIMEOUT ||
                event.getReason() == DiscordDisconnectedEvent.Reason.MISSED_PINGS) {
            try {
                discordReconnectManager.reconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
