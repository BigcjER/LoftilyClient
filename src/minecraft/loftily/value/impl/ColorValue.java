package loftily.value.impl;

import loftily.value.Value;

import java.awt.*;

public class ColorValue extends Value<Color> {
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
}
