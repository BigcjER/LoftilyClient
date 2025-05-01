package loftily.value.impl;

import loftily.value.Value;
import lombok.Getter;

@Getter
public class NumberValue extends Value<Double> {
    private final double minValue, maxValue, step;

    public NumberValue(String name, double value, double minValue, double maxValue, double step) {
        super(name, value);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
    }

    public NumberValue(String name, double value, double minValue, double maxValue) {
        this(name, value, minValue, maxValue, 1);
    }
}
