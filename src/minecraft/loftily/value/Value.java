package loftily.value;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
public abstract class Value<T, V> {
    private final String name;
    private final T defaultValue;
    protected T value;
    private Supplier<Boolean> visible;
    
    public Value(String name, T value) {
        this.name = name;
        this.value = value;
        this.defaultValue = value;
    }
    
    @SuppressWarnings("unchecked")
    public V setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
        return (V) this;
    }
    
    public abstract JsonElement write();
    
    public abstract Value<T, V> read(JsonElement element);
}
