package loftily.config.impl.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import loftily.Client;
import loftily.config.api.JsonConfig;
import loftily.module.AutoDisableType;
import loftily.module.Module;
import loftily.settings.ClientSettings;
import loftily.value.Value;
import loftily.value.impl.mode.Mode;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileReader;

public class ModuleJsonConfig extends JsonConfig {
    
    public ModuleJsonConfig() {
        super(null/* ClientSettings会设置 */);
    }
    
    @Override
    protected void read(JsonObject json, FileReader reader) {
        json.entrySet().forEach(entry -> {
            Module module = Client.INSTANCE.getModuleManager().get(entry.getKey());
            if (module == null) return;
            
            JsonObject modJson = entry.getValue().getAsJsonObject();
            if (modJson.get("Toggled").getAsBoolean() && !module.isToggled())
                module.setToggled(true, false, false, false);
            
            String autoDisable = modJson.get("AutoDisable").getAsString();
            if (!autoDisable.equals(AutoDisableType.NONE.name))
                module.setAutoDisableType(AutoDisableType.fromName(autoDisable));
            
            module.setKey(Keyboard.getKeyIndex(modJson.get("KeyBind").getAsString()));
            
            
            JsonObject valuesJson = modJson.getAsJsonObject("values");
            
            valuesJson.entrySet().forEach(valueEntry -> {
                String key = valueEntry.getKey();
                JsonElement element = valueEntry.getValue();
                //是嵌套Mode的，遍历里面的Value
                if (element.isJsonObject()) {
                    JsonObject modeValuesJson = element.getAsJsonObject();
                    modeValuesJson.entrySet().forEach(valueInModeEntry -> {
                        String valueInModeName = valueInModeEntry.getKey();
                        
                        Value<?, ?> valueToRead = module.getValueInMode(valueInModeName, key);
                        if (valueToRead != null) {
                            valueToRead.read(valueInModeEntry.getValue());
                        }
                    });
                    return;
                }
                
                //是顶层的value
                Value<?, ?> valueToRead = module.getTopLevelValue(key);
                
                if (valueToRead != null) {
                    valueToRead.read(element);
                }
            });
        });
        
    }
    
    
    @Override
    protected void write(JsonObject json) {
        Client.INSTANCE.getModuleManager().getAll().forEach(module -> {
            JsonObject modJson = new JsonObject();
            modJson.addProperty("Toggled", module.isToggled());
            modJson.addProperty("KeyBind", Keyboard.getKeyName(module.getKey()));
            modJson.addProperty("AutoDisable", module.getAutoDisableType().name);
            
            JsonObject valuesJson = new JsonObject();
            module.getValues().forEach(value -> {
                //给Mode添加嵌套
                if (value.getParentMode() != null) {
                    JsonObject jsonObjectForMode = new JsonObject();
                    
                    Mode<?> mode = value.getParentMode();
                    mode.getValues().forEach(valueInMode -> jsonObjectForMode.add(valueInMode.getName(), valueInMode.write()));
                    valuesJson.add(mode.getName(), jsonObjectForMode);
                    return;
                }
                
                //不在mode下
                valuesJson.add(value.getName(), value.write());
            });
            modJson.add("values", valuesJson);
            
            json.add(module.getName(), modJson);
        });
    }
    
    public void load(File configFile) {
        write();
        Client.INSTANCE.getModuleManager().getAll().forEach(module -> module.setToggled(false, false, false, false));
        this.file = configFile;
        read();
        
        ClientSettings.lastModuleConfig.set(configFile.getName());
        Client.INSTANCE.getFileManager().get(ClientSettingsJsonConfig.class).write();
    }
}
