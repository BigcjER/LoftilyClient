package loftily.settings;

import java.util.function.BiConsumer;

public class FieldProxy<T> {
    private final BiConsumer<T, T> onChange;
    private T value;
    
    public FieldProxy(T initialValue, BiConsumer<T, T> onChange) {
        this.value = initialValue;
        this.onChange = onChange;
    }
    
    public T get() {
        return value;
    }
    
    public void set(T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        if (onChange != null) {
            onChange.accept(oldValue, newValue);
        }
    }
}