package com.netply.zero.discord.persistence;

import java.util.List;

public interface TrackedUserManager {
    void addUser(String user);

    List<String> getAllTrackedUsers();
}
