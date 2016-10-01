package com.netply.zero.discord.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrackedUserManagerImpl implements TrackedUserManager {
    private static List<String> trackedUsers = new ArrayList<>();


    @Override
    public void addUser(String user) {
        if (!trackedUsers.contains(user)) {
            trackedUsers.add(user);
        }
    }

    @Override
    public List<String> getAllTrackedUsers() {
        return trackedUsers.stream().distinct().collect(Collectors.toList());
    }
}
