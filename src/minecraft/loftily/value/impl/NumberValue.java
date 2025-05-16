package loftily.value.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import loftily.value.Value;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;

@Getter
public class NumberValue extends Value<Double, NumberValue> {
    private final double minValue, maxValue, step;
    private NumberValue maxWith, minWith;
    
    public NumberValue(String name, double value, double minValue, double maxValue, double step) {
        super(name, value);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
    }
    
    public NumberValue(String name, double value, double minValue, double maxValue) {
        this(name, value, minValue, maxValue, 1);
    }
    
    @Override
    public JsonElement write() {
        return new JsonPrimitive(value);
    }
    
    @Override
    public Value<Double, NumberValue> read(JsonElement element) {
        if (element.isJsonPrimitive()) {
            value = element.getAsDouble();
        }
        return this;
    }
    
    public NumberValue setMaxWith(NumberValue maxWith) {
        this.maxWith = maxWith;
        return this;
    }
    
    public NumberValue setMinWith(NumberValue minWith) {
        this.minWith = minWith;
        return this;
    }
    
    @Override
    public void setValue(Double value) {
        if (minWith != null) value = MathHelper.clamp(value, minWith.getValue(), maxValue);
        if (maxWith != null) value = MathHelper.clamp(value, minValue, maxWith.getValue());
        
        super.setValue(value);
    }
}
