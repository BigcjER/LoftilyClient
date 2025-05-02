package loftily.core;

import loftily.utils.client.MinecraftInstance;
import loftily.value.Value;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class AbstractModule implements MinecraftInstance {
    protected final List<Value<?>> values = new ArrayList<>();
    protected String name;
    
    public void onEnable() {
    }
    
    public void onDisable() {
    }
    
    public void onToggle() {
    }
    
}
