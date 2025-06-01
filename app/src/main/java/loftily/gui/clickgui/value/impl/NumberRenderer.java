package loftily.gui.clickgui.value.impl;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.clickgui.value.ValueRenderer;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.value.impl.NumberValue;
import org.lwjgl.input.Keyboard;

public class NumberRenderer extends ValueRenderer<NumberValue> {
    private final Animation sliderAnimation;
    private boolean dragging, hovering;
    
    public NumberRenderer(NumberValue value) {
        super(value, 25);
        this.sliderAnimation = new Animation(Easing.EaseOutExpo, 300);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        dragging = hovering = false;
        Keyboard.enableRepeatEvents(false);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        //RenderUtils.drawRoundedRect(x, y, width, height, 0, new Color(255, 0, 0, 80));
        hovering = RenderUtils.isHovering(mouseX, mouseY, x, y, width, height);
        
        float startX = x + 6;
        float sliderWidth = width - 12;
        
        //文字部分
        font.drawString(value.getName(), startX, y + 3.5F, Colors.Text.color);
        String num = formatNumber(value.getValue());
        font.drawString(num, x + width - 7 - font.getStringWidth(num), y + 3.5F, Colors.Text.color);
        
        //滑块部分
        float sliderY = y + 16;
        float sliderHeight = 5;
        float sliderRadius = (sliderHeight - 0.1F) / 2;
        RenderUtils.drawRoundedRect(startX, sliderY, sliderWidth, sliderHeight, sliderRadius, Colors.BackGround.color);
        
        double min = value.getMinValue();
        double max = value.getMaxValue();
        double current = value.getValue();
        float activeWidth = (float) (sliderWidth * ((current - min) / (max - min)));
        
        sliderAnimation.run(activeWidth);
        
        //激活部分
        RenderUtils.drawRoundedRect(
                startX - 0.3f,
                sliderY,
                sliderAnimation.getValuef() - 0.2F,
                sliderHeight,
                sliderRadius - 0.35F,
                Colors.Active.color);
        
        //滑块指示器
        float thumbSize = sliderHeight + 1;
        float thumbX = startX + sliderAnimation.getValuef() - thumbSize / 2;
        thumbX = Math.min(thumbX, startX + sliderWidth - thumbSize);
        RenderUtils.drawRoundedRectOutline(
                thumbX,
                sliderY - (thumbSize - sliderHeight) / 2,
                thumbSize,
                thumbSize,
                thumbSize / 2 - 0.2F,
                0.6f,
                Colors.Active.color,
                Colors.Text.color
        );
        
        //拖动逻辑
        if (dragging) {
            double clampedX = Math.max(startX, Math.min(mouseX, startX + sliderWidth));
            
            double percent = (clampedX - startX) / sliderWidth;
            double newValue = getNewValue(value.getMinValue(), value.getMaxValue(), value.getStep(), percent);
            
            value.setValue(Math.round(newValue * 100) / 100.0);
        }
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;
        
        if (hovering) dragging = true;
    }
    
    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = false;
    }
    
    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        if (!hovering) return;
        double currentValue = value.getValue();
        double step = value.getStep();
        double minValue = value.getMinValue();
        double maxValue = value.getMaxValue();
        double newValue;
        
        switch (keyCode) {
            case Keyboard.KEY_LEFT:
                newValue = currentValue - step;
                newValue = clampAndRound(newValue, minValue, maxValue, step);
                value.setValue(Math.round(newValue * 100.0) / 100.0);
                break;
            
            case Keyboard.KEY_RIGHT:
                newValue = currentValue + step;
                newValue = clampAndRound(newValue, minValue, maxValue, step);
                value.setValue(Math.round(newValue * 100.0) / 100.0);
                break;
        }
    }
    
    private double getNewValue(double min, double max, double step, double percent) {
        double rawValue = min + percent * (max - min);
        rawValue = Math.round(rawValue / step) * step;
        rawValue = Math.max(min, Math.min(rawValue, max));
        return Math.round(rawValue * 100.0) / 100.0;
    }
    
    private double clampAndRound(double valueToClamp, double min, double max, double step) {
        double clampedValue = Math.max(min, Math.min(valueToClamp, max));
        double roundedValue = Math.round(clampedValue / step) * step;
        return Math.round(roundedValue * 100.0) / 100.0;
    }
    
    private String formatNumber(double value) {
        return value % 1 == 0 ?
                String.valueOf((int) value) :
                String.format("%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}