package loftily.gui.clickgui.value.impl;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.clickgui.value.ValueRenderer;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.value.impl.RangeSelectionNumberValue;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

public class RangeSelectionNumberRenderer extends ValueRenderer<RangeSelectionNumberValue> {
    private final Animation leftAnim, rightAnim;
    private DraggingState draggingState = DraggingState.NONE;
    private float dragOffset;
    private boolean hovering;
    private int lastMouseX;
    
    public RangeSelectionNumberRenderer(RangeSelectionNumberValue value) {
        super(value, 25);
        this.leftAnim = new Animation(Easing.EaseOutExpo, 300);
        this.rightAnim = new Animation(Easing.EaseOutExpo, 300);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        draggingState = DraggingState.NONE;
        hovering = false;
        Keyboard.enableRepeatEvents(false);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.lastMouseX = mouseX;
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
        if (draggingState != DraggingState.NONE) {
            float mousePercent = (mouseX - startX - dragOffset) / sliderWidth;
            mousePercent = Math.max(0, Math.min(1, mousePercent));
            
            double newValue = min + mousePercent * (max - min);
            double step = value.getStep();
            newValue = Math.round(newValue / step) * step;
            
            switch (draggingState) {
                case LEFT:
                    value.setFirst(Math.min(newValue, value.getSecond()));
                    break;
                case RIGHT:
                    value.setSecond(Math.max(newValue, value.getFirst()));
                    break;
                case MIDDLE:
                    double range = value.getSecond() - value.getFirst();
                    double rawCenterValue = min + mousePercent * (max - min);
                    double alignedCenter = Math.round(rawCenterValue / value.getStep()) * value.getStep();
                    
                    double minPossibleCenter = min + range / 2;
                    double maxPossibleCenter = max - range / 2;
                    alignedCenter = MathHelper.clamp(alignedCenter, minPossibleCenter, maxPossibleCenter);
                    
                    double newFirst = alignedCenter - range / 2;
                    double newSecond = alignedCenter + range / 2;
                    
                    value.setFirst(Math.round(newFirst / value.getStep()) * value.getStep());
                    value.setSecond(Math.round(newSecond / value.getStep()) * value.getStep());
                    break;
            }
        }
        
    }
    
    //微调部分
    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        if (!hovering) return;
        
        double min = value.getMinValue();
        double max = value.getMaxValue();
        double step = value.getStep();
        double first = value.getFirst();
        double second = value.getSecond();
        
        float startX = x + 6;
        float sliderWidth = width - 12;
        float leftThumbPos = startX + (float) ((first - min) / (max - min) * sliderWidth);
        float rightThumbPos = startX + (float) ((second - min) / (max - min) * sliderWidth);
        
        MouseRegion region;
        if (lastMouseX < leftThumbPos) {
            region = MouseRegion.LEFT_EMPTY;
        } else if (lastMouseX > rightThumbPos) {
            region = MouseRegion.RIGHT_EMPTY;
        } else {
            region = MouseRegion.ACTIVE_RANGE;
        }
        
        switch (keyCode) {
            case Keyboard.KEY_LEFT: {
                switch (region) {
                    case LEFT_EMPTY:
                        double newFirst = Math.max(min, first - step);
                        value.setFirst(Math.round(newFirst / step) * step);
                        break;
                    case ACTIVE_RANGE:
                        if (first > min) {
                            double range = second - first;
                            double newFirstRange = Math.max(min, first - step);
                            double newSecondRange = newFirstRange + range;
                            value.setFirst(Math.round(newFirstRange / step) * step);
                            value.setSecond(Math.round(newSecondRange / step) * step);
                        }
                        break;
                    case RIGHT_EMPTY:
                        double newSecond = Math.max(first, second - step);
                        value.setSecond(Math.round(newSecond / step) * step);
                        break;
                }
                break;
            }
            case Keyboard.KEY_RIGHT: {
                switch (region) {
                    case LEFT_EMPTY:
                        double newFirst = Math.min(second, first + step);
                        value.setFirst(Math.round(newFirst / step) * step);
                        break;
                    case ACTIVE_RANGE:
                        if (second < max) {
                            double range = second - first;
                            double newSecondRange = Math.min(max, second + step);
                            double newFirstRange = newSecondRange - range;
                            value.setFirst(Math.round(newFirstRange / step) * step);
                            value.setSecond(Math.round(newSecondRange / step) * step);
                        }
                        break;
                    case RIGHT_EMPTY:
                        double newSecond = Math.min(max, second + step);
                        value.setSecond(Math.round(newSecond / step) * step);
                        break;
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
            draggingState = DraggingState.LEFT;
            dragOffset = mouseX - currentLeft;
        } else if (Math.abs(mouseX - currentRight) < 8) {
            draggingState = DraggingState.RIGHT;
            dragOffset = mouseX - currentRight;
        } else if (mouseX > currentLeft && mouseX < currentRight) {
            draggingState = DraggingState.MIDDLE;
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
        draggingState = DraggingState.NONE;
    }
    
    private String formatNumber(double value) {
        return value % 1 == 0 ?
                String.valueOf((int) value) :
                String.format("%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
    
    private enum DraggingState {
        NONE, LEFT, RIGHT, MIDDLE
    }
    
    private enum MouseRegion {
        LEFT_EMPTY, ACTIVE_RANGE, RIGHT_EMPTY
    }
}