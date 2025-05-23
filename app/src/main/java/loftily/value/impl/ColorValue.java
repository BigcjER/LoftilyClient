package loftily.value.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import loftily.value.Value;

import java.awt.*;

public class ColorValue extends Value<Color, ColorValue> {
    public ColorValue(String name, Color value) {
        super(name, value);
    }
    
    public ColorValue(String name, int red, int green, int blue) {
        this(name, new Color(red, green, blue));
    }
    
    public void setRed(int red) {
        setValue(new Color(red, getValue().getGreen(), getValue().getBlue()));
    }
    
    public void setGreen(int green) {
        setValue(new Color(getValue().getRed(), green, getValue().getBlue()));
    }
    
    public void setBlue(int blue) {
        setValue(new Color(getValue().getBlue(), getValue().getGreen(), blue));
    }
    
    @Override
    public JsonElement write() {
        JsonArray array = new JsonArray();
        array.add(value.getRed());
        array.add(value.getGreen());
        array.add(value.getBlue());
        return array;
    }
    
    @Override
    public Value<Color, ColorValue> read(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            
            if (array.size() == 3) {
                int red = array.get(0).getAsInt();
                int green = array.get(1).getAsInt();
                int blue = array.get(2).getAsInt();
                
                Color color = new Color(red, green, blue);
                
                this.setValue(color);
            }
        }
        
        return this;
    }
}
