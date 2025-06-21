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
    private final FontRenderer valueFont = FontManager.NotoSans.of(14);
    private final Animation expandAnimation;
    
    private final int NOT_EXPANDED_BOX_HEIGHT = 11;
    
    private boolean expanded;
    
    private float boxX, boxY;
    private float boxWidth = 63;
    
    public MultiBooleanRenderer(MultiBooleanValue value) {
        super(value, 15);
        this.expandAnimation = new Animation(Easing.EaseOutQuart, 300);
        this.expanded = false;
        
        int longestWidth = 0;
        for (Map.Entry<String, Boolean> entry : value.getValue().entrySet()) {
            String key = entry.getKey();
            int width = font.getStringWidth(key);
            if (width > longestWidth) longestWidth = width;
        }
        
        boxWidth = Math.max(boxWidth, longestWidth + 10);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        float textX = x + 6;
        font.drawString(value.getName(), textX, y + 3.5F, Colors.Text.color);
        
        boxX = x + width - boxWidth - ClickGui.PADDING;
        boxY = y + 2;
        
        //如果box过长，换行
        int PADDING = 2;
        if (boxX < x + 6 + font.getWidth(value.getName())) {
            boxX = textX;
            boxY = y + NOT_EXPANDED_BOX_HEIGHT + PADDING + 1;//+1
        }
        
        expandAnimation.run(expanded ? value.getValue().size() * NOT_EXPANDED_BOX_HEIGHT : 0);
        height = Math.max(NOT_EXPANDED_BOX_HEIGHT + PADDING * 2, (boxY - y + NOT_EXPANDED_BOX_HEIGHT) + PADDING) + expandAnimation.getValuef();
        
        float boxHeight = NOT_EXPANDED_BOX_HEIGHT + expandAnimation.getValuef();
        RenderUtils.drawRoundedRect(boxX, boxY, boxWidth, boxHeight, ClickGui.CORNER_RADIUS - 1, Colors.BackGround.color);
        
        //获取所有启用的Boolean
        List<String> toggleValues = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : value.getValue().entrySet()) {
            if (entry.getValue()) toggleValues.add(entry.getKey());
        }
        
        //绘制所有启用的Boolean在没展开的Box上
        StringBuilder sb = new StringBuilder();
        for (String toggledName : toggleValues) {
            sb.append(toggledName);
            if (toggleValues.indexOf(toggledName) != toggleValues.size() - 1) sb.append(",");
        }
        int maxWidth = (int) (boxWidth - 17);
        String stringToDraw = valueFont.trimStringToWidth(sb.toString(), maxWidth, false);
        if (valueFont.getWidth(stringToDraw) >= maxWidth) stringToDraw = stringToDraw + "...";
        valueFont.drawString(stringToDraw, boxX + boxWidth / 2F - valueFont.getWidth(stringToDraw) / 2F, boxY + 2.5F, Colors.Text.color.getRGB());
        
        //绘制展开项
        if (expandAnimation.isFinished() && !expanded) return;
        RenderUtils.startGlScissor((int) boxX, (int) boxY, (int) boxWidth, (int) boxHeight);
        float textYOffset = 0;
        for (Map.Entry<String, Boolean> entry : value.getValue().entrySet()) {
            String name = entry.getKey();
            boolean value = entry.getValue();
            
            valueFont.drawString(
                    name,
                    boxX + boxWidth / 2F - (float) valueFont.getStringWidth(name) / 2,
                    boxY + PADDING + NOT_EXPANDED_BOX_HEIGHT + textYOffset,
                    value ? Colors.Text.color : Colors.Text.color.darker());
            
            textYOffset += NOT_EXPANDED_BOX_HEIGHT;
        }
        RenderUtils.stopGlScissor();
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0 && mouseButton != 1) return;
        
        if (RenderUtils.isHovering(mouseX, mouseY, x, y, width, boxY - y + NOT_EXPANDED_BOX_HEIGHT))
            expanded = !expanded;
        
        if (!expanded) return;
        
        
        float textYOffset = 0;
        for (Map.Entry<String, Boolean> entry : value.getValue().entrySet()) {
            if (RenderUtils.isHovering(mouseX, mouseY,
                    boxX,
                    boxY + NOT_EXPANDED_BOX_HEIGHT + textYOffset,
                    boxWidth,
                    NOT_EXPANDED_BOX_HEIGHT)) {
                entry.setValue(!entry.getValue());
            }
            textYOffset += NOT_EXPANDED_BOX_HEIGHT;
        }
    }
}
