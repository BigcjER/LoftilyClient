package loftily.config.impl.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import loftily.config.FileManager;
import loftily.config.api.JsonConfig;
import loftily.gui.interaction.draggable.IDraggable;
import loftily.handlers.impl.render.DraggableHandler;

import java.io.File;
import java.io.FileReader;

public class DragsJsonConfig extends JsonConfig {
    public DragsJsonConfig() {
        super(new File(FileManager.ROOT_DIR, "drags.json"));
    }
    
    @Override
    protected void read(JsonObject json, FileReader reader) {
        
        json.entrySet().forEach(entry -> {
            IDraggable draggable = DraggableHandler.get(entry.getKey());
            if (draggable.getDraggable() == null) return;
            
            JsonArray jsonArray = entry.getValue().getAsJsonArray();
            
            float x = jsonArray.get(0).getAsFloat();
            float y = jsonArray.get(1).getAsFloat();
            
            draggable.getDraggable().setRelativeX(x);
            draggable.getDraggable().setRelativeY(y);
        });
    }
    
    @Override
    protected void write(JsonObject json) {
        DraggableHandler.getDraggableList().forEach(draggable -> {
            if (draggable == null || draggable.getDraggable() == null) return;
            json.add(draggable.getName(), GSON.toJsonTree(new float[]{draggable.getDraggable().getRelativeX(), draggable.getDraggable().getRelativeY()}));
        });
    }
}