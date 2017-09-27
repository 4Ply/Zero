package com.netply.zero.discord;

import com.netply.zero.discord.live.DiscordReconnectManager;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;

import java.util.logging.Logger;

public class DiscordDisconnectEventListener implements IListener<DisconnectedEvent> {
    private DiscordReconnectManager discordReconnectManager;


    public DiscordDisconnectEventListener(DiscordReconnectManager discordReconnectManager) {
        this.discordReconnectManager = discordReconnectManager;
    }

    @Override
    public void handle(DisconnectedEvent event) {
        Logger.getGlobal().info("Disconnected (" + event.getReason().name() + ")");
        if (event.getReason() == DisconnectedEvent.Reason.ABNORMAL_CLOSE ||
                event.getReason() == DisconnectedEvent.Reason.RECONNECT_OP ||
                event.getReason() == DisconnectedEvent.Reason.LOGGED_OUT) {
            try {
                discordReconnectManager.reconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
