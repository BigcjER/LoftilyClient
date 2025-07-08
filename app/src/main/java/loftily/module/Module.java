package loftily.module;

import loftily.Client;
import loftily.config.impl.json.ModuleJsonConfig;
import loftily.core.AbstractModule;
import loftily.gui.notification.NotificationType;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.mode.Mode;
import loftily.value.impl.mode.ModeValue;
import loftily.value.impl.mode.StringMode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public abstract class Module extends AbstractModule {
    private final boolean defaultToggled, canBeToggled;
    private final ModuleCategory moduleCategory;
    private final int defaultKey;
    @Setter
    private AutoDisableType autoDisableType;
    private boolean toggled;
    private int key;
    
    public Module() {
        if (!this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            throw new RuntimeException(String.format("ModuleInfo not found in %s!", this.getClass().getSimpleName()));
        }
        ModuleInfo moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);
        this.name = moduleInfo.name();
        this.key = moduleInfo.key();
        this.defaultKey = key;
        this.moduleCategory = moduleInfo.category();
        this.defaultToggled = moduleInfo.defaultToggled();
        this.canBeToggled = moduleInfo.canBeToggled();
        this.autoDisableType = moduleInfo.autoDisable();
    }
    
    public void setToggled(boolean toggled, boolean save, boolean notification) {
        this.toggled = toggled;
        
        if (!canBeToggled) {
            if (toggled && mc.player != null) onEnable();
            this.toggled = false;
            if (mc.player != null) onDisable();
            return;
        }
        
        /* 注册Modes */
        this.values.stream()
                .filter(value -> value instanceof ModeValue)
                .map(value -> (ModeValue) value)
                .flatMap(modeValue -> modeValue.getModes().stream()
                        .filter(mode -> !(mode instanceof StringMode) && mode.equals(modeValue.getValue())))
                .forEach(toggled ? Mode::register : Mode::unregister);
        
        this.values.stream()
                .filter(value -> value instanceof BooleanValue)
                .forEach(value -> {
                    final BooleanValue booleanValue = (BooleanValue) value;
                    if (booleanValue.getMode() != null) {
                        if (this.toggled && booleanValue.getValue()) booleanValue.getMode().register();
                        else booleanValue.getMode().unregister();
                    }
                });
        
        if (notification)
            Client.INSTANCE.getNotificationManager().add(
                    NotificationType.INFO,
                    "ModuleManager",
                    String.format("%s %s", name, toggled ? "Enabled" : "Disabled"),
                    1500);
        
        
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
        
        if (save) Client.INSTANCE.getFileManager().get(ModuleJsonConfig.class).write();
        
    }
    
    public void toggle() {
        setToggled(!toggled, true, true);
    }
    
    public void setKey(int key) {
        int oldKey = this.key;
        
        if (oldKey == key) return;
        
        this.key = key;
        
        Client.INSTANCE.getModuleManager().handelUpdateModuleKeybind(this, oldKey, key);
    }
    
    public @NonNull String getTag() {
        return "";
    }
}
