package loftily.value.impl.mode;

import loftily.Client;
import loftily.core.AbstractModule;
import loftily.module.Module;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Mode extends AbstractModule {
    private Module parent;
    
    public Mode(String name) {
        this.name = name;
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
    
    public void toggle() {
        getParent().toggle();
    }
}
