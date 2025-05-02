package loftily.file.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import loftily.Client;
import loftily.file.ConfigManager;
import loftily.file.Config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;

public class ClientSettings {
    public static String lastConfig = "default.json";
    public static boolean isDarkMode = false;
    
    public static class ClientSettingsConfig extends Config {
        public ClientSettingsConfig() {
            super(new File(ConfigManager.rootDir, "ClientSettings.json"));
        }
        
        /** Make sure it's loaded before {@link ModuleConfig} */
        @Override
        protected int getPriority() {
            return -10;
        }
        
        @Override
        public void init() {
            super.init();
            ModuleConfig moduleConfig = Client.INSTANCE.getConfigManager().get(ModuleConfig.class);
            
            File lastConfigFile = new File(ConfigManager.configDir,lastConfig);
            moduleConfig.setConfigFile(lastConfigFile);
        }
        
        @Override
        public void read() {
            try (FileReader reader = new FileReader(configFile)) {
                JsonElement jsonElement = new JsonParser().parse(reader);
                
                if(jsonElement instanceof JsonNull) {
                    write();
                    return;
                }
                
                JsonObject json = jsonElement.getAsJsonObject();
                
                for (Field field : ClientSettings.class.getDeclaredFields()) {
                    if (json.has(field.getName())) {
                        Object obj = GSON.fromJson(json.get(field.getName()), field.getType());
                        field.set(null, obj);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public void write() {
            try (FileWriter writer = new FileWriter(configFile)) {
                JsonObject json = new JsonObject();
                for (Field field : ClientSettings.class.getDeclaredFields()) {
                    json.add(field.getName(), GSON.toJsonTree(field.get(null)));
                }
                GSON.toJson(json, writer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}