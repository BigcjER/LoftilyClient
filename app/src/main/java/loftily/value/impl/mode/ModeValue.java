package loftily.value.impl.mode;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import loftily.core.AbstractModule;
import loftily.module.Module;
import loftily.value.Value;
import lombok.Getter;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public class ModeValue extends Value<Mode, ModeValue> {
    
    private final List<Mode> modes;
    private final AbstractModule parent;
    
    /**
     * @param parent the parent module (typically just 'this')
     */
    public ModeValue(String name, String value, AbstractModule parent, Mode... modes) {
        super(name, Arrays.stream(modes)
                .filter(mode -> mode.getName().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Mode '%s' not found!", value))));
        this.modes = Arrays.asList(modes);
        this.parent = parent;
    }
    
    
    public void update(Mode value) {
        if (getValue().getParent() != null && getValue().getParent().isToggled()) {
            if (!(getValue() instanceof StringMode)) getValue().unregister();
            setValue(value);
            if (!(getValue() instanceof StringMode)) getValue().register();
        } else {
            setValue(value);
        }
    }
    
    public String getValueByName() {
        return getValue().getName();
    }
    
    public boolean is(String mode) {
        return mode.equalsIgnoreCase(getValueByName());
    }
    
    public void initModes() {
        for (Mode mode : modes) {
            mode.setParent(parent instanceof Module ? (Module) parent : null);
            
            Field[] fields = mode.getClass().getDeclaredFields();
            
            /* Add the value of a sub mode */
            for (Field field : fields) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Value<?, ?> value = (Value<?, ?>) field.get(mode);
                        value.setParentMode(mode);
                        mode.getValues().add(value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            parent.getValues().addAll(mode.getValues());
        }
        
        /* Set the visible of a sub mode */
        modes.forEach(mode -> mode.getValues().forEach(value -> {
            Supplier<Boolean> visible = value.getVisible();
            
            value.setVisible(visible == null
                    ? () -> mode == this.getValue()
                    : () -> visible.get() && (mode == this.getValue()));
        }));
    }
    
    @Override
    public JsonElement write() {
        return new JsonPrimitive(getValue().getName());
    }
    
    @Override
    public Value<Mode, ModeValue> read(JsonElement element) {
        if (element.isJsonPrimitive()) {
            String modeName = element.getAsString();
            for (Mode mode : modes) {
                if (mode.getName().equalsIgnoreCase(modeName)) {
                    update(mode);
                    break;
                }
            }
        }
        return this;
    }
    
    @Override
    public String handleCommand(String valueToSetText) {
        //Find has match mode and getter
        Optional<Mode> matchedModeOptional = modes.stream()
                .filter(mode -> mode.getName().equalsIgnoreCase(valueToSetText))
                .findFirst();
        
        if (!matchedModeOptional.isPresent()) {
            return TextFormatting.RED + String.format("%s doesn't have mode %s!\nModes:\n%s",
                    getName(),
                    valueToSetText,
                    modes.stream()
                            .map(Mode::getName)
                            .collect(Collectors.joining(", ")));
        }
        
        Mode mode = matchedModeOptional.get();
        update(mode);
        
        return String.format("%s is set to %s.", getName(), mode.getName());
    }
}