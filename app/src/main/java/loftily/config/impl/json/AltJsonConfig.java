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
        JsonElement alts = jsonObject.get("alts");
        
        
        if (alts != null && alts.isJsonArray()) {
            JsonArray altsArray = alts.getAsJsonArray();
            Type listType = new TypeToken<List<Alt>>() {
            }.getType();
            
            List<Alt> altList = GSON.fromJson(altsArray, listType);
            
            if (altList != null) {
                altList.forEach(alt -> Client.INSTANCE.getAltManager().getAlts().add(alt));
            } else {
                write();
            }
        }
    }
    
    @Override
    protected void write(JsonObject jsonObject) {
        JsonArray jsonArray = new JsonArray();
        Client.INSTANCE.getAltManager().getAlts().forEach(alt -> {
            JsonObject modJson = new JsonObject();
            modJson.addProperty("name", alt.getName());
            if (alt.getType() == AltType.Microsoft) {
                modJson.addProperty("uuid", alt.getUuid());
                modJson.addProperty("refreshToken", alt.getRefreshToken());
            }
            modJson.addProperty("type", alt.getType().toString());
            
            jsonArray.add(modJson);
        });
        
        jsonObject.add("alts", jsonArray);
    }
}
