package loftily.value;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
public abstract class Value<T> {
    private final String name;
    private final T defaultValue;
    protected T value;
    private Supplier<Boolean> visible;

    public Value(String name, T value) {
        this.name = name;
        this.value = value;
        this.defaultValue = value;
    }

    public Value<T> setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
        return this;
    }
}
