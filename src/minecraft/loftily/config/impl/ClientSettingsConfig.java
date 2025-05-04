package loftily.config.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import loftily.Client;
import loftily.config.Config;
import loftily.config.ConfigManager;
import loftily.settings.ClientSettings;
import loftily.settings.FieldProxy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public class ClientSettingsConfig extends Config {
    public ClientSettingsConfig() {
        super(new File(ConfigManager.rootDir, "ClientSettings.json"));
    }
    
    //确保比ModuleConfig和ThemeConfig先加载
    @Override
    protected int getPriority() {
        return 10;
    }
    
    @Override
    public void init() {
        super.init();
        ModuleConfig moduleConfig = Client.INSTANCE.getConfigManager().get(ModuleConfig.class);
        
        File lastConfigFile = new File(ConfigManager.configDir, ClientSettings.lastModuleConfig.get());
        moduleConfig.setConfigFile(lastConfigFile);
    }
    
    @Override
    public void read() {
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            
            for (Field field : ClientSettings.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                
                if (json.has(field.getName())) {
                    if (FieldProxy.class.isAssignableFrom(field.getType())) {
                        /* 获取反射的字段类型 */
                        Type fieldType = ((ParameterizedType) field.getGenericType())
                                .getActualTypeArguments()[0];
                        
                        /* 反序列化值 */
                        Object value = GSON.fromJson(json.get(field.getName()), fieldType);
                        
                        FieldProxy fieldProxy = (FieldProxy<?>) field.get(null);
                        if (fieldProxy != null) fieldProxy.set(value);
                    } else {
                        field.set(null, GSON.fromJson(json.get(field.getName()), field.getType()));
                    }
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
                if (!Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                
                if (FieldProxy.class.isAssignableFrom(field.getType())) {
                    FieldProxy<?> fieldProxy = (FieldProxy<?>) field.get(null);
                    json.add(field.getName(), GSON.toJsonTree(fieldProxy.get()));
                } else {
                    json.add(field.getName(), GSON.toJsonTree(field.get(null)));
                }
            }
            
            GSON.toJson(json, writer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}