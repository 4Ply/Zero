package com.netply.zero.discord.live;

import sx.blah.discord.util.DiscordException;

public interface DiscordReconnectManager {
    void reconnect() throws DiscordException;
}
