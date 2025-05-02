package loftily.value.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import loftily.value.Value;
import lombok.Getter;

@Getter
public class NumberValue extends Value<Double> {
    private final double minValue, maxValue, step;
    
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
    public Value<Double> read(JsonElement element) {
        if (element.isJsonPrimitive()) {
            value = element.getAsDouble();
        }
        return this;
    }
}
