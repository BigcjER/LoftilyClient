package loftily.gui.clickgui.value.impl;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.clickgui.value.ValueRenderer;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.value.impl.RangeSelectionNumberValue;
import net.minecraft.util.math.MathHelper;

public class RangeSelectionNumberRenderer extends ValueRenderer<RangeSelectionNumberValue> {
    private final Animation leftAnim, rightAnim;
    private DraggingState draggingState = DraggingState.None;
    private float dragOffset;
    private boolean hovering;
    
    public RangeSelectionNumberRenderer(RangeSelectionNumberValue value) {
        super(value, 25);
        this.leftAnim = new Animation(Easing.EaseOutExpo, 300);
        this.rightAnim = new Animation(Easing.EaseOutExpo, 300);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        hovering = RenderUtils.isHovering(mouseX, mouseY, x, y, width, height);
        
        float startX = x + 6;
        float sliderWidth = width - 12;
        float sliderY = y + 16;
        float sliderHeight = 5;
        float sliderRadius = (sliderHeight - 0.1F) / 2;
        
        //文字部分
        String valueText = formatNumber(value.getFirst()) + " - " + formatNumber(value.getSecond());
        font.drawString(value.getName(), startX, y + 3.5F, Colors.Text.color);
        font.drawString(valueText, x + width - 7 - font.getStringWidth(valueText), y + 3.5F, Colors.Text.color);
        
        //计算数值范围
        double min = value.getMinValue();
        double max = value.getMaxValue();
        double currentMin = value.getFirst();
        double currentMax = value.getSecond();
        
        float leftPos = (float) ((currentMin - min) / (max - min) * sliderWidth);
        float rightPos = (float) ((currentMax - min) / (max - min) * sliderWidth);
        
        //更新动画
        leftAnim.run(leftPos);
        rightAnim.run(rightPos);
        float animatedLeft = leftAnim.getValuef();
        float animatedRight = rightAnim.getValuef();
        
        //背景
        RenderUtils.drawRoundedRect(startX, sliderY, sliderWidth, sliderHeight, sliderRadius, Colors.BackGround.color);
        
        //激活区域
        if (animatedRight > animatedLeft) {
            RenderUtils.drawRoundedRect(
                    startX + animatedLeft,
                    sliderY,
                    animatedRight - animatedLeft,
                    sliderHeight,
                    sliderRadius,
                    Colors.Active.color
            );
        }
        
        //绘制左滑块指示器
        drawThumb(startX + animatedLeft, sliderY, sliderHeight);
        //绘制右滑块指示器
        drawThumb(startX + animatedRight, sliderY, sliderHeight);
        
        //拖动逻辑
        if (draggingState != DraggingState.None) {
            float mousePercent = (mouseX - startX - dragOffset) / sliderWidth;
            mousePercent = Math.max(0, Math.min(1, mousePercent));
            
            double newValue = min + mousePercent * (max - min);
            double step = value.getStep();
            newValue = Math.round(newValue / step) * step;
            
            switch (draggingState) {
                case Left:
                    value.setFirst(Math.min(newValue, value.getSecond()));
                    break;
                case Right:
                    value.setSecond(Math.max(newValue, value.getFirst()));
                    break;
                case Middle:
                    double range = value.getSecond() - value.getFirst();
                    double alignedValue = Math.round(newValue / value.getStep()) * value.getStep();
                    
                    double newFirst = Math.max(min, alignedValue - range / 2);
                    double newSecond = Math.min(max, alignedValue + range / 2);
                    
                    newFirst = Math.round(newFirst / value.getStep()) * value.getStep();
                    newSecond = Math.round(newSecond / value.getStep()) * value.getStep();
                    
                    if (newFirst > min && newSecond < max) {
                        value.setFirst(newFirst);
                        value.setSecond(newSecond);
                    }
                    break;
            }
        }
        
    }
    
    private void drawThumb(float x, float y, float trackHeight) {
        float size = 6.0f;
        
        RenderUtils.drawRoundedRectOutline(
                x - size / 2,
                y - (size - trackHeight) / 2,
                size,
                size,
                size / 2,
                0.6f,
                Colors.Active.color,
                Colors.Text.color
        );
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0 || !hovering) return;
        
        float startX = x + 6;
        float sliderWidth = width - 12;
        float currentLeft = startX + leftAnim.getValuef();
        float currentRight = startX + rightAnim.getValuef();
        
        //点击左侧空白区域
        if (mouseX < currentLeft - 4) {
            double newValue = calculateValueFromMouse(startX, sliderWidth, mouseX);
            value.setFirst(Math.min(newValue, value.getSecond()));
            return;
        }
        
        //点击右侧空白区域
        if (mouseX > currentRight + 4) {
            double newValue = calculateValueFromMouse(startX, sliderWidth, mouseX);
            value.setSecond(Math.max(newValue, value.getFirst()));
            return;
        }
        
        //点击检测逻辑
        if (Math.abs(mouseX - currentLeft) < 8) {
            draggingState = DraggingState.Left;
            dragOffset = mouseX - currentLeft;
        } else if (Math.abs(mouseX - currentRight) < 8) {
            draggingState = DraggingState.Right;
            dragOffset = mouseX - currentRight;
        } else if (mouseX > currentLeft && mouseX < currentRight) {
            draggingState = DraggingState.Middle;
            dragOffset = mouseX - (currentLeft + (currentRight - currentLeft) / 2);
        }
    }
    
    //根据鼠标位置计算数值
    private double calculateValueFromMouse(float startX, float sliderWidth, int mouseX) {
        double min = value.getMinValue();
        double max = value.getMaxValue();
        float percent = MathHelper.clamp((mouseX - startX) / sliderWidth, 0, 1);
        double rawValue = min + percent * (max - min);
        return Math.round(rawValue / value.getStep()) * value.getStep();
        
    }
    
    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        draggingState = DraggingState.None;
    }
    
    private String formatNumber(double value) {
        return value % 1 == 0 ?
                String.valueOf((int) value) :
                String.format("%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
    
    private enum DraggingState {
        None, Left, Right, Middle
    }
}