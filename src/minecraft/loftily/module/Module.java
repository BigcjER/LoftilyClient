package loftily.module;

import loftily.Client;
import loftily.core.AbstractModule;
import loftily.file.impl.ModuleConfig;
import loftily.value.impl.mode.Mode;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Module extends AbstractModule {
    private ModuleCategory moduleCategory;
    private boolean toggled;
    @Setter
    private int key;
    
    public Module() {
        if (!this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            throw new RuntimeException(String.format("ModuleInfo not found in %s!", this.getClass().getSimpleName()));
        }
        ModuleInfo moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);
        if (moduleInfo != null) {
            this.name = moduleInfo.name();
            this.key = moduleInfo.key();
            this.moduleCategory = moduleInfo.category();
        }
    }
    
    public void setToggled(boolean toggled, boolean save) {
        this.toggled = toggled;
        if (save) Client.INSTANCE.getConfigManager().get(ModuleConfig.class).write();
    }
    
    public void toggle() {
        setToggled(!toggled, true);
        
        /* register mode */
        this.values.stream()
                .filter(value -> value instanceof ModeValue)
                .map(value -> (ModeValue) value)
                .flatMap(modeValue -> modeValue.getModes().stream()
                        .filter(mode -> !(mode instanceof StringMode) && mode.equals(modeValue.getValue())))
                .forEach(toggled ? Mode::register : Mode::unregister);
        
        if (toggled) {
            Client.INSTANCE.getEventManager().register(this);
            if (mc.player != null) {
                onEnable();
                onToggle();
            }
        } else {
            Client.INSTANCE.getEventManager().unregister(this);
            if (mc.player != null) {
                onDisable();
                onToggle();
            }
        }
    }
}
