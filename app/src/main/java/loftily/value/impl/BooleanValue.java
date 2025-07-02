package loftily.value.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import loftily.module.Module;
import loftily.value.Value;
import loftily.value.impl.mode.Mode;
import loftily.value.impl.mode.StringMode;
import lombok.Getter;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.function.Supplier;

@Getter
public class BooleanValue extends Value<Boolean, BooleanValue> {
    private final Mode mode;
    private final Module parent;
    
    public BooleanValue(String name, Boolean value) {
        this(name, value, null, null);
    }
    
    public BooleanValue(String name, Boolean value, Module parent, Mode mode) {
        super(name, value);
        this.mode = mode;
        this.parent = parent;
    }
    
    public void initMode() {
        if (parent == null || mode == null || mode instanceof StringMode) return;
        
        mode.setParent(parent);
        Field[] fields = mode.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Value.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Value<?, ?> subValue = (Value<?, ?>) field.get(mode);
                    subValue.setParentMode(mode);
                    mode.getValues().add(subValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        
        parent.getValues().addAll(mode.getValues());
        
        mode.getValues().forEach(subValue -> {
            Supplier<Boolean> visible = subValue.getVisible();
            
            subValue.setVisible(visible == null
                    ? this::getValue
                    : () -> visible.get() && this.getValue());
        });
    }
    
    @Override
    public JsonElement write() {
        return new JsonPrimitive(value);
    }
    
    @Override
    public void setValue(Boolean value) {
        super.setValue(value);
        
        if (this.mode != null && this.parent != null) {
            if (this.parent.isToggled()) {
                if (this.getValue()) {
                    this.mode.register();
                } else {
                    this.mode.unregister();
                }
            }
        }
    }
    
    @Override
    public Value<Boolean, BooleanValue> read(JsonElement element) {
        if (element.isJsonPrimitive()) {
            value = element.getAsBoolean();
        }
        return this;
    }
    
    @Override
    public String handleCommand(String valueToSetText) {
        switch (valueToSetText.toLowerCase()) {
            case "true":
                setValue(true);
                break;
            case "false":
                setValue(false);
                break;
            
            default:
                return TextFormatting.RED + valueToSetText + " is not a valid value for boolean type.";
        }
        
        return String.format("%s is set to %s.", getName(), value);
    }
}
