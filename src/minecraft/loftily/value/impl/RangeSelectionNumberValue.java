package loftily.value.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import loftily.utils.math.Pair;
import loftily.value.Value;
import lombok.Getter;

@Getter
public class RangeSelectionNumberValue extends Value<Pair<Double, Double>, RangeSelectionNumberValue> {
    private final double minValue, maxValue, step;
    
    /**
     * @param second must bigger or equal to @param first
     */
    public RangeSelectionNumberValue(String name, double first, double second, double minValue, double maxValue, double step) {
        super(name, new Pair<>(Math.min(first, second), second));
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
    }
    
    public RangeSelectionNumberValue(String name, double first, double second, double minValue, double maxValue) {
        this(name,first,second,minValue,maxValue,1);
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
    
    @Override
    public JsonElement write() {
        JsonArray array = new JsonArray();
        array.add(getFirst());
        array.add(getSecond());
        return array;
    }
    
    @Override
    public Value<Pair<Double, Double>, RangeSelectionNumberValue> read(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() == 2) {
                try {
                    value.setFirst(array.get(0).getAsDouble());
                    value.setSecond(array.get(1).getAsDouble());
                } catch (NumberFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return this;
    }
}
