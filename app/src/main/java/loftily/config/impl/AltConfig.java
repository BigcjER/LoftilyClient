package loftily.config.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import loftily.Client;
import loftily.alt.Alt;
import loftily.alt.AltType;
import loftily.config.Config;
import loftily.config.FileManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class AltConfig extends Config {
    public AltConfig() {
        super(new File(FileManager.ROOT_DIR, "Alts.json"));
    }
    
    @Override
    public void read() {
        try (FileReader reader = new FileReader(configFile)) {
            Type listType = new TypeToken<List<Alt>>() {
            }.getType();
            List<Alt> altList = GSON.fromJson(reader, listType);
            
            if (altList != null) {
                altList.forEach(alt -> Client.INSTANCE.getAltManager().getAlts().add(alt));
            } else {
                write();
            }
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void write() {
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
        
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(GSON.toJson(jsonArray));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
