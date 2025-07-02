package loftily.value.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import loftily.utils.client.ClientUtils;
import loftily.value.Value;
import net.minecraft.util.text.TextFormatting;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultiBooleanValue extends Value<Map<String, Boolean>, MultiBooleanValue> {
    /**
     * Use the {@link #add(String, boolean)} method to add multiple BooleanValues.
     */
    public MultiBooleanValue(String name) {
        super(name, new LinkedHashMap<String, Boolean>() {
            //Override it for value command
            @Override
            public String toString() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\n");
                for (Map.Entry<String, Boolean> entry : this.entrySet()) {
                    stringBuilder
                            .append(entry.getKey())
                            .append(" ")
                            .append(entry.getValue())
                            .append("\n");
                }
                
                if (stringBuilder.length() > 0) {
                    stringBuilder.setLength(stringBuilder.length() - 1);
                }
                
                return stringBuilder.toString();
            }
        });
    }
    
    public MultiBooleanValue add(String name, boolean value) {
        if (getValue().containsKey(name)) {
            throw new IllegalArgumentException(String.format("Key '%s' already exists!", name));
        }
        getValue().put(name, value);
        return this;
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
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, Boolean> entry : getValue().entrySet()) {
            jsonObject.addProperty(entry.getKey(), entry.getValue());
        }
        return jsonObject;
    }
    
    @Override
    public Value<Map<String, Boolean>, MultiBooleanValue> read(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            Map<String, Boolean> newValues = new LinkedHashMap<>(getValue());
            
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (newValues.containsKey(entry.getKey())) {
                    newValues.put(entry.getKey(), entry.getValue().getAsBoolean());
                } else {
                    ClientUtils.LOGGER.warn("Found unknown key '{}' in value '{}'. It will be ignored.", entry.getKey(), this.getName());
                }
            }
            super.setValue(newValues);
        }
        return this;
    }
    
    @Override
    public String handleCommand(String valueToSetText) {
        String[] parts = valueToSetText.split(",");
        if (parts.length != 2) {
            return "Usage: .module <a_multi_boolean_value> <name>,<true|false>";
        }
        
        String key = parts[0];
        String value = parts[1];
        
        //Check contains
        if (!getValue().containsKey(key)) {
            return TextFormatting.RED + String.format("%s doesn't have key %s!", getName(), key);
        }
        
        //Check is valid boolean
        if (!value.matches("true|false")) {
            return TextFormatting.RED + valueToSetText + " is not a valid value for boolean type.";
        }
        
        setValue(key, Boolean.parseBoolean(value));
        return String.format("Key %s in %s is set to %s", key, getName(), value);
    }
}
