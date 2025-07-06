package loftily.value.impl.mode;

import loftily.Client;
import loftily.core.AbstractModule;
import loftily.module.Module;
import loftily.utils.other.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Mode<T extends Module> extends AbstractModule {
    private T parent;
    
    
    public Mode(String name) {
        this.name = name;
        if(StringUtils.PATTERN_WHITESPACE.matcher(name).find()) {
            throw new IllegalArgumentException(String.format("Value name '%s' cannot contain spaces.", name));
        }
    }
    
    public final void register() {
        Client.INSTANCE.getEventManager().register(this);
        if (mc.player != null) {
            this.onEnable();
            this.onToggle();
        }
    }
    
    public final void unregister() {
        Client.INSTANCE.getEventManager().unregister(this);
        if (mc.player != null) {
            this.onDisable();
            this.onToggle();
        }
    }
}
