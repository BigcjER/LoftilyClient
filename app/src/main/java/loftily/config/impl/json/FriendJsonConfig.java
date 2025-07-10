package loftily.config.impl.json;

import com.google.gson.JsonObject;
import loftily.config.FileManager;
import loftily.config.api.JsonConfig;
import loftily.handlers.impl.client.FriendsHandler;

import java.io.File;
import java.io.FileReader;
import java.util.UUID;

public class FriendJsonConfig extends JsonConfig {
    public FriendJsonConfig() {
        super(new File(FileManager.ROOT_DIR, "friends.json"));
    }
    
    @Override
    protected void read(JsonObject jsonObject, FileReader reader) {
        jsonObject.entrySet().forEach(entry -> {
            UUID uuid = UUID.fromString(entry.getKey());
            String name = entry.getValue().getAsJsonObject().get("name").getAsString();
            
            FriendsHandler.add(name, uuid);
        });
    }
    
    @Override
    protected void write(JsonObject jsonObject) {
        FriendsHandler.getFriends().forEach((name, uuid) -> {
            JsonObject jsonMod = new JsonObject();
            
            jsonMod.addProperty("name", name);
            
            jsonObject.add(uuid.toString(), jsonMod);
        });
    }
}
