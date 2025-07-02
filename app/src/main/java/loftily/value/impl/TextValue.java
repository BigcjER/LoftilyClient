package loftily.value.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import loftily.value.Value;

public class TextValue extends Value<String, TextValue> {
    public TextValue(String name, String value) {
        super(name, value);
    }
    
    @Override
    public JsonElement write() {
        return new JsonPrimitive(value);
    }
    
    @Override
    public Value<String, TextValue> read(JsonElement element) {
        if (element.isJsonPrimitive()) {
            value = element.getAsString();
        }
        return this;
    }
    
    @Override
    public String handleCommand(String valueToSetText) {
        setValue(valueToSetText);
        return String.format("%s is set to %s", getName(), getValue());
    }
}
