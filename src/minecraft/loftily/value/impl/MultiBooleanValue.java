package loftily.value.impl;

import loftily.value.Value;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultiBooleanValue extends Value<Map<String, Boolean>> {
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
}
