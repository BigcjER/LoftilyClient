package loftily.gui.clickgui.value.impl;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.clickgui.value.ValueRenderer;
import loftily.gui.font.FontManager;
import loftily.gui.font.FontRenderer;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.value.impl.MultiBooleanValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiBooleanRenderer extends ValueRenderer<MultiBooleanValue> {
    private final int BoxWidth = 63;
    private final int BoxHeight = 11;
    private final Animation expandAnimation;
    private boolean expanded;
    private final FontRenderer valueFont = FontManager.NotoSans.of(14);
    
    public MultiBooleanRenderer(MultiBooleanValue value) {
        super(value, 15);
        this.expandAnimation = new Animation(Easing.EaseOutQuart, 300);
        this.expanded = false;
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        font.drawString(value.getName(), x + 6, y + 3.5F, Colors.Text.color);
        
        expandAnimation.run(expanded ? (value.getValue().size()) * BoxHeight : BoxHeight);
        height = expandAnimation.getValuef() + 4F;
        
        float BoxX = x + width - BoxWidth - ClickGui.PADDING;
        float BoxY = y + 2;
        float modeBoxHeight = expandAnimation.getValuef();
        
        RenderUtils.drawRoundedRect(BoxX, BoxY, BoxWidth, modeBoxHeight, ClickGui.CORNER_RADIUS - 1, Colors.BackGround.color);
        
        
        //获取所有启用的Boolean，绘制在没展开的Box上
        List<String> toggleValues = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : value.getValue().entrySet())
            if (entry.getValue()) toggleValues.add(entry.getKey());
        
        StringBuilder sb = new StringBuilder();
        for (String toggledName : toggleValues) {
            sb.append(toggledName);
            if (toggleValues.indexOf(toggledName) != toggleValues.size() - 1) sb.append(",");
        }
        int maxWidth = BoxWidth - 15;
        String stringToDraw = font.trimStringToWidth(sb.toString(), maxWidth, false);
        if (font.getWidth(stringToDraw) >= maxWidth) stringToDraw = stringToDraw + "...";
        
        valueFont.drawString(stringToDraw, BoxX + BoxWidth / 2F - valueFont.getWidth(stringToDraw) / 2F, BoxY + 2.5F, Colors.Text.color.getRGB());
        
        
        //展开后所有的Boolean
        RenderUtils.startGlScissor((int) BoxX, (int) BoxY, BoxWidth, (int) modeBoxHeight);
        float textYOffset = 0;
        for (Map.Entry<String, Boolean> entry : value.getValue().entrySet()) {
            String name = entry.getKey();
            boolean value = entry.getValue();
            
            valueFont.drawString(
                    name,
                    BoxX + BoxWidth / 2F - (float) valueFont.getStringWidth(name) / 2,
                    BoxY + 2F + BoxHeight + textYOffset,
                    value ? Colors.Text.color : Colors.Text.color.darker());
            textYOffset += BoxHeight;
        }
        RenderUtils.stopGlScissor();
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if ((mouseButton != 0 && mouseButton != 1)) return;
        
        if (RenderUtils.isHovering(mouseX, mouseY, x, y, width, BoxHeight))
            expanded = !expanded;
        
        if (!expanded) return;
        
        float BoxX = x + width - BoxWidth - ClickGui.PADDING;
        float BoxY = y + 2;
        
        float textYOffset = 0;
        for (Map.Entry<String, Boolean> entry : value.getValue().entrySet()) {
            if (RenderUtils.isHovering(mouseX, mouseY,
                    BoxX,
                    BoxY + 2F + BoxHeight + textYOffset,
                    BoxWidth, BoxHeight))
                entry.setValue(!entry.getValue());
            
            textYOffset += BoxHeight;
        }
    }
}
