package loftily.config.api;

import com.google.gson.*;
import loftily.utils.client.ClientUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public abstract class JsonConfig extends ManagedFile implements Comparable<JsonConfig> {
    
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public JsonConfig(File file) {
        super(file);
    }
    
    public final void read() {
        try (FileReader reader = new FileReader(file)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            
            if (jsonElement == null || jsonElement.isJsonNull() || !jsonElement.isJsonObject()) {
                throw new JsonParseException(String.format("File '%s' is not a valid JsonObject.", file.getPath()));
            }
            
            read(jsonElement.getAsJsonObject(), reader);
            
        } catch (Exception e) {
            ClientUtils.LOGGER.error("Failed to read config : {}, Creating a new one.", file.getPath());
            e.printStackTrace();
            write();
        }
    }
    
    public final void write() {
        JsonObject json = new JsonObject();
        
        write(json);
        
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config : " + file.getPath(), e);
        }
    }
    
    protected abstract void read(JsonObject json, FileReader reader);
    
    protected abstract void write(JsonObject json);
    
    public void init() {
        if (file != null)
            if (file.exists()) read();
            else write();
    }
    
    protected int getPriority() {
        return 0;
    }
    
    @Override
    public int compareTo(JsonConfig other) {
        return Integer.compare(other.getPriority(), this.getPriority());
    }
}