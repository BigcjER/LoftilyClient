package loftily.core;

import loftily.utils.client.ClientUtils;
import loftily.value.Value;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class AbstractModule implements ClientUtils {
    protected final List<Value> values = new ArrayList<>();
    protected String name;
    
    public void onEnable() {
    }
    
    public void onDisable() {
    }
    
    public void onToggle() {
    }
    
    public Value<?, ?> getValueInMode(String name, String modeName) {
        if (modeName == null) {
            return getTopLevelValue(name);
        }
        
        return this.getValues().stream()
                .filter(v -> v.getParentMode() != null &&
                        v.getParentMode().getName().equalsIgnoreCase(modeName) &&
                        v.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    public Value<?, ?> getTopLevelValue(String name) {
        return this.getValues().stream()
                .filter(v -> v.getParentMode() == null && v.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
