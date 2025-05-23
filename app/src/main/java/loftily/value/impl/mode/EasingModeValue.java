package loftily.value.impl.mode;

import loftily.core.AbstractModule;
import loftily.gui.animation.Easing;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class EasingModeValue extends ModeValue {
    private static final Map<String, Mode> ModeMap = Arrays.stream(Easing.values())
            .collect(Collectors.toMap(Easing::name,
                    easing -> new StringMode(easing.name())
            ));
    
    public EasingModeValue(String name, Easing value, AbstractModule parent) {
        super(name, value.toString(), parent, ModeMap.values().toArray(new Mode[0]));
    }
    
    public Easing getValueByEasing() {
        String modeName = getValueByName();
        return Easing.valueOf(modeName);
    }
    
    /**
     * @deprecated Use {@link #setValue(Easing)} instead.
     */
    @Deprecated
    public void setValue(Mode value) {
        if (ModeMap.containsKey(value.getName())) {
            super.setValue(ModeMap.get(value.getName()));
        }
    }
    
    public void setValue(Easing value) {
        Mode mode = ModeMap.get(value.name());
        if (mode != null) {
            super.setValue(mode);
        }
    }
    
    @Override
    public void update(Mode value) {
        if (getValue().getParent() != null && getValue().getParent().isToggled()) {
            if (!(getValue() instanceof StringMode)) getValue().unregister();
            super.setValue(ModeMap.get(value.getName()));
            if (!(getValue() instanceof StringMode)) getValue().register();
        } else {
            super.setValue(ModeMap.get(value.getName()));
        }
    }
}
