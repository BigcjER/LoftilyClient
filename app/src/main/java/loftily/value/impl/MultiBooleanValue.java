package loftily.value.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import loftily.utils.other.StringUtils;
import loftily.value.Value;
import net.minecraft.util.text.TextFormatting;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
        if (StringUtils.PATTERN_WHITESPACE.matcher(name).find()) {
            throw new IllegalArgumentException(String.format("Value name '%s' cannot contain spaces.", name));
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
        //使用JsonArray避免与Mode冲突
        JsonArray jsonArray = new JsonArray();
        for (Map.Entry<String, Boolean> entry : getValue().entrySet()) {
            if (entry.getValue()) {
                jsonArray.add(entry.getKey());
            }
        }
        
        return jsonArray;
    }
    
    @Override
    public Value<Map<String, Boolean>, MultiBooleanValue> read(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray jsonArray = element.getAsJsonArray();
            //查找在Json里启用的value
            Set<String> enabledFromJson = new HashSet<>();
            for (JsonElement jsonElement : jsonArray) {
                if (jsonElement.isJsonPrimitive() && ((JsonPrimitive) jsonElement).isString()) {
                    enabledFromJson.add(jsonElement.getAsString());
                }
            }
            
            
            Map<String, Boolean> newValues = new LinkedHashMap<>();
            
            for (String key : getValue().keySet()) {
                //避免添加没有的key
                if (getValue().containsKey(key))
                    newValues.put(key, enabledFromJson.contains(key));
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
