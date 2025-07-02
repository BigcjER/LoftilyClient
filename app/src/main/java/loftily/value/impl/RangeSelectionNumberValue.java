package loftily.value.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import loftily.utils.client.MessageUtils;
import loftily.utils.math.Pair;
import loftily.value.Value;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.math.NumberUtils;

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
        this(name, first, second, minValue, maxValue, 1);
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
    
    @Override
    public String handleCommand(String valueToSetText) {
        String[] parts = valueToSetText.split("-");
        if (parts.length != 2) {
            return "Usage: .module <a_range_selection_number_value> <first_number>-<second_number>";
        }
        
        String first = parts[0];
        String second = parts[1];
        
        if (!NumberUtils.isParsable(first)) {
            return TextFormatting.RED + String.format("The first value '%s' is not a valid number.", first);
        }
        if (!NumberUtils.isParsable(second)) {
            return TextFormatting.RED + String.format("The second value '%s' is not a valid number.", second);
        }
        
        double firstAsDouble = Double.parseDouble(first);
        double secondAsDouble = Double.parseDouble(second);
        firstAsDouble = Math.round(firstAsDouble * 100) / 100.0;
        secondAsDouble = Math.round(secondAsDouble * 100) / 100.0;
        
        if (MathHelper.clamp(firstAsDouble, minValue, maxValue) != firstAsDouble) {
            MessageUtils.clientMessageWithWaterMark(String.format("%sWarning: %s is out of range.", TextFormatting.YELLOW, firstAsDouble));
        }
        if (MathHelper.clamp(secondAsDouble, minValue, maxValue) != secondAsDouble) {
            MessageUtils.clientMessageWithWaterMark(String.format("%sWarning: %s is out of range.", TextFormatting.YELLOW, secondAsDouble));
        }
        
        setFirst(firstAsDouble);
        setSecond(secondAsDouble);
        
        return String.format("%s is set to %s-%s", getName(), getFirst(), getSecond());
    }
}
