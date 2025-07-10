package loftily.handlers.impl.client;


import loftily.Client;
import loftily.config.impl.json.FriendJsonConfig;
import loftily.handlers.Handler;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class FriendsHandler extends Handler {
    private static final Map<String, UUID> friends = new LinkedHashMap<>();
    
    public static Map<String, UUID> getFriends() {
        return Collections.unmodifiableMap(friends);
    }
    
    public static boolean contains(String name, UUID uuid) {
        return friends.get(name) == uuid;
    }
    
    public static void remove(String name, UUID uuid) {
        friends.remove(name, uuid);
        
        if (Client.INSTANCE.getFileManager() != null)
            Client.INSTANCE.getFileManager().get(FriendJsonConfig.class).write();
    }
    
    public static void add(String name, UUID uuid) {
        friends.put(name, uuid);
        
        if (Client.INSTANCE.getFileManager() != null)
            Client.INSTANCE.getFileManager().get(FriendJsonConfig.class).write();
    }
    
    @Override
    protected boolean needRegister() {
        return false;
    }
}
