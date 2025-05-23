package loftily.value.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import loftily.value.Value;

public class BooleanValue extends Value<Boolean, BooleanValue> {
    public BooleanValue(String name, Boolean value) {
        super(name, value);
    }
    
    @Override
    public JsonElement write() {
        return new JsonPrimitive(value);
    }
    
    @Override
    public Value<Boolean, BooleanValue> read(JsonElement element) {
        if (element.isJsonPrimitive()) {
            value = element.getAsBoolean();
        }
        return this;
    }
}
