package loftily.config.impl.json;

import com.google.gson.JsonObject;
import loftily.Client;
import loftily.config.FileManager;
import loftily.config.api.JsonConfig;
import loftily.settings.ClientSettings;
import loftily.settings.FieldProxy;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public class ClientSettingsJsonConfig extends JsonConfig {
    public ClientSettingsJsonConfig() {
        super(new File(FileManager.ROOT_DIR, "client-settings.json"));
    }
    
    //确保比ModuleConfig先加载
    @Override
    protected int getPriority() {
        return 10;
    }
    
    @Override
    public void init() {
        super.init();
        ModuleJsonConfig moduleJsonConfig = Client.INSTANCE.getFileManager().get(ModuleJsonConfig.class);
        
        File lastConfigFile = new File(FileManager.CONFIG_DIR, ClientSettings.lastModuleConfig.get());
        moduleJsonConfig.setFile(lastConfigFile);
    }
    
    @Override
    protected void read(JsonObject json, FileReader reader) {
        try {
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
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void write(JsonObject json) {
        try {
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
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}