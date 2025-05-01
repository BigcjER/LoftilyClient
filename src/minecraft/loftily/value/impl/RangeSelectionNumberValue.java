package loftily.value.impl;

import loftily.utils.math.Pair;
import loftily.value.Value;

public class RangeSelectionNumberValue extends Value<Pair<Double, Double>> {
    /**
     * @param second must bigger or equal to @param first
     */
    public RangeSelectionNumberValue(String name, Double first, Double second) {
        super(name, new Pair<>(Math.min(first, second), second));
    }

    public double getFirst() {
        return value.getFirst();
    }

    public void setFirst(double first) {
        getValue().setFirst(Math.min(first, value.getSecond()));
    }

    public double getSecond() {
        return value.getSecond();
    }

    public void setSecond(double second) {
        getValue().setSecond(Math.max(getFirst(), second));
    }
}
