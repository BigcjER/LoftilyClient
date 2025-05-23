package loftily.value.impl;

import com.google.gson.JsonElement;
import loftily.value.Value;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultiBooleanValue extends Value<Map<String, Boolean>, MultiBooleanValue> {
    /**
     * Use the {@link #add(String, boolean)} method to add multiple BooleanValues.
     */
    public MultiBooleanValue(String name) {
        super(name, new LinkedHashMap<>());
    }
    
    public MultiBooleanValue add(String name, boolean value) {
        if (getValue().containsKey(name)) {
            throw new IllegalArgumentException(String.format("Key '%s' already exists!", name));
        }
        getValue().put(name, value);
        return this;
    }
    
    /**
     * @deprecated Use {@link #setValue(String, boolean)} instead.
     */
    @Deprecated
    public void setValue(Map<String, Boolean> value) {
        throw new UnsupportedOperationException("Use setValue(String, boolean) instead");
    }
    
    public boolean getValue(String key) {
        if (getValue().containsKey(key)) {
            return value.get(key);
        } else {
            throw new IllegalArgumentException(String.format("Key '%s' not found!", key));
        }
    }
    
    
    public void setValue(String name, boolean value) {
        if (!getValue().containsKey(name)) {
            throw new IllegalArgumentException(String.format("Key '%s' doesn't exists!", name));
        }
        getValue().put(name, value);
    }
    
    @Override
    public JsonElement write() {
        com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
        for (Map.Entry<String, Boolean> entry : getValue().entrySet()) {
            jsonObject.addProperty(entry.getKey(), entry.getValue());
        }
        return jsonObject;
    }
    
    @Override
    public Value<Map<String, Boolean>, MultiBooleanValue> read(JsonElement element) {
        if (element.isJsonObject()) {
            com.google.gson.JsonObject jsonObject = element.getAsJsonObject();
            Map<String, Boolean> newValues = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                newValues.put(entry.getKey(), entry.getValue().getAsBoolean());
            }
            super.setValue(newValues);
        }
        return this;
    }
}
