package loftily.config.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import loftily.config.Config;
import loftily.config.FileManager;
import loftily.gui.interaction.draggable.IDraggable;
import loftily.handlers.impl.DraggableHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class DragsConfig extends Config {
    public DragsConfig() {
        super(new File(FileManager.ROOT_DIR, "Drags.json"));
    }
    
    @Override
    public void read() {
        try (FileReader reader = new FileReader(configFile)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            checkJsonElement(jsonElement);
            
            JsonObject json = jsonElement.getAsJsonObject();
            
            json.entrySet().forEach(entry -> {
                IDraggable draggable = DraggableHandler.get(entry.getKey());
                if (draggable.getDraggable() == null) return;
                
                JsonArray jsonArray = entry.getValue().getAsJsonArray();
                
                int x = jsonArray.get(0).getAsInt();
                int y = jsonArray.get(1).getAsInt();
                
                draggable.getDraggable().setPosX(x);
                draggable.getDraggable().setPosY(y);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void write() {
        try (FileWriter writer = new FileWriter(configFile)) {
            JsonObject json = new JsonObject();
            
            DraggableHandler.getDraggableList().forEach(draggable -> {
                if (draggable == null || draggable.getDraggable() == null) return;
                json.add(draggable.getName(), GSON.toJsonTree(new int[]{draggable.getDraggable().getPosX(), draggable.getDraggable().getPosY()}));
            });
            
            GSON.toJson(json, writer);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
