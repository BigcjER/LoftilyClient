package loftily.value;

import com.google.gson.JsonElement;
import loftily.value.impl.mode.Mode;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@Setter
@SuppressWarnings("unchecked")
public abstract class Value<T, V> {
    private Consumer<? super T> onValueChange = null;
    private final String name;
    private final T defaultValue;
    protected T value;
    private Supplier<Boolean> visible;
    private Mode<?> parentMode = null;
    
    public Value(String name, T value) {
        this.name = name;
        this.value = value;
        this.defaultValue = value;
    }
    
    public V setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
        return (V) this;
    }
    
    public V setOnValueChange(Consumer<? super T> onValueChange) {
        this.onValueChange = onValueChange;
        return (V) this;
    }
    
    public void setValue(T value) {
        if (!this.value.equals(value)) {
            this.value = value;
            if (onValueChange != null) onValueChange.accept(value);
        }
    }
    
    public abstract JsonElement write();
    
    public abstract Value<T, V> read(JsonElement element);
}
