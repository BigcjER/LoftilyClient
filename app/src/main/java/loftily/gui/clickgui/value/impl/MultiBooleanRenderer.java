package loftily.gui.clickgui.value.impl;

import loftily.gui.clickgui.ClickGui;
import loftily.gui.clickgui.value.ValueRenderer;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.value.impl.MultiBooleanValue;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiBooleanRenderer extends ValueRenderer<MultiBooleanValue> {
    private final float BoxWidth = 63;
    private final float BoxHeight = 11;
    
    public MultiBooleanRenderer(MultiBooleanValue value) {
        super(value, 15);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        font.drawString(value.getName(), x + 6, y + height / 2 - font.getHeight() / 3F, Colors.Text.color);
        
        float BoxX = x + width - BoxWidth - ClickGui.Padding;
        float BoxY = y + 2;
        FontRenderer valueFont = FontManager.NotoSans.of(14);
        
        RenderUtils.drawRoundedRect(BoxX, BoxY, BoxWidth, BoxHeight, ClickGui.CornerRadius - 1, Colors.BackGround.color);
        
        List<String> toggleValues = new ArrayList<>();
        
        for (Map.Entry<String, Boolean> entry : value.getValue().entrySet()) {
            if (entry.getValue()) toggleValues.add(entry.getKey());
        }
        
        int textXOffset = 0;
        for (String toggledName : toggleValues) {
            if (toggleValues.indexOf(toggledName) != toggleValues.size() - 1) toggledName += ", ";
            valueFont.drawString(toggledName, BoxX + ClickGui.Padding + textXOffset, BoxY + 2, new Color(0, 0, 0).getRGB());
            textXOffset += valueFont.getStringWidth(toggledName);
        }
    }
}
