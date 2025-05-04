package loftily.config.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import loftily.Client;
import loftily.config.Config;
import loftily.module.Module;
import loftily.settings.ClientSettings;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModuleConfig extends Config {
    
    public ModuleConfig() {
        super(null/* ClientSettings会设置 */);
    }
    
    @Override
    public void read() {
        try (FileReader reader = new FileReader(configFile)) {
            JsonElement jsonElement = new JsonParser().parse(reader);
            
            if (jsonElement instanceof JsonNull) {
                write();
                return;
            }
            
            JsonObject json = jsonElement.getAsJsonObject();
            
            json.entrySet().forEach(entry -> {
                Module module = Client.INSTANCE.getModuleManager().get(entry.getKey());
                if (module == null) return;
                
                JsonObject modJson = entry.getValue().getAsJsonObject();
                if (modJson.get("Toggled").getAsBoolean() && !module.isToggled()) {
                    module.setToggled(true, false);
                }
                
                module.setKey(Keyboard.getKeyIndex(modJson.get("KeyBind").getAsString()));
                
                module.getValues().forEach(value -> {
                    if (modJson.has(value.getName())) {
                        value.read(modJson.get(value.getName()));
                    }
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    @Override
    public void write() {
        JsonObject json = new JsonObject();
        
        Client.INSTANCE.getModuleManager().forEach(module -> {
            JsonObject modJson = new JsonObject();
            modJson.addProperty("Toggled", module.isToggled());
            modJson.addProperty("KeyBind", Keyboard.getKeyName(module.getKey()));
            module.getValues().forEach(value -> modJson.add(value.getName(), value.write()));
            
            json.add(module.getName(), modJson);
        });
        
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(GSON.toJson(json));
            Client.Logger.info("Written ModuleConfig to {}", configFile.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void load(File configFile) {
        write();
        Client.INSTANCE.getModuleManager().forEach(module -> module.setToggled(false, false));
        this.configFile = configFile;
        read();
        
        ClientSettings.lastModuleConfig.set(configFile.getName());
        Client.INSTANCE.getConfigManager().get(ClientSettingsConfig.class).write();
    }
}
