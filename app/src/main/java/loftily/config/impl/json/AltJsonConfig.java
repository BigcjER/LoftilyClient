package loftily.config.impl.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import loftily.Client;
import loftily.alt.Alt;
import loftily.alt.AltType;
import loftily.config.FileManager;
import loftily.config.api.JsonConfig;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

public class AltJsonConfig extends JsonConfig {
    public AltJsonConfig() {
        super(new File(FileManager.ROOT_DIR, "alts.json"));
    }
    
    @Override
    protected void read(JsonObject jsonObject, FileReader reader) {
        JsonElement microsoft = jsonObject.get("microsoft");
        JsonElement offline = jsonObject.get("offline");
        
        for (JsonElement microsoftElement : microsoft.getAsJsonArray()) {
            JsonObject altObject = microsoftElement.getAsJsonObject();
            
            String name = altObject.get("name").getAsString();
            String uuid = altObject.get("uuid").getAsString();
            String refreshToken = altObject.get("refreshToken").getAsString();
            
            Client.INSTANCE.getAltManager().add(new Alt(name,uuid,refreshToken));
        }
        
        for (JsonElement offlineElement : offline.getAsJsonArray()) {
            JsonObject altObject = offlineElement.getAsJsonObject();
            
            String name = altObject.get("name").getAsString();
            
            Client.INSTANCE.getAltManager().add(new Alt(name));
        }
    }
    
    @Override
    protected void write(JsonObject jsonObject) {
        JsonArray jsonArrayMicrosoft = new JsonArray();
        JsonArray jsonArrayOffline = new JsonArray();
        
        Client.INSTANCE.getAltManager().getAlts().forEach(alt -> {
            JsonObject modJson = new JsonObject();
            modJson.addProperty("name", alt.getName());
            if (alt.getType() == AltType.Microsoft) {
                modJson.addProperty("uuid", alt.getUuid());
                modJson.addProperty("refreshToken", alt.getRefreshToken());
                modJson.addProperty("type", alt.getType().toString());
                jsonArrayMicrosoft.add(modJson);
            }
            
            if (alt.getType() == AltType.Offline) {
                jsonArrayOffline.add(modJson);
            }
        });
        
        jsonObject.add("microsoft", jsonArrayMicrosoft);
        jsonObject.add("offline", jsonArrayOffline);
    }
}
