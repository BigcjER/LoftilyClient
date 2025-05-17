package loftily.gui.clickgui.value;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.clickgui.value.impl.*;
import loftily.gui.font.FontManager;
import loftily.gui.interaction.Scrollable;
import loftily.module.Module;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import loftily.value.Value;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.MultiBooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.RangeSelectionNumberValue;
import loftily.value.impl.mode.ModeValue;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ValuePanel {
    public final Animation animation;
    private final List<ValueRenderer<?>> valueRenderers;
    private final Module module;
    private final Scrollable scrollable;
    public boolean out;
    @Setter
    private float x, y, width, height;
    
    public ValuePanel(Module module) {
        this.module = module;
        this.valueRenderers = new ArrayList<>();
        this.animation = new Animation(Easing.EaseOutQuart, 300);
        this.scrollable = new Scrollable(8);
        
        for (Value<?, ?> value : module.getValues()) {
            if (value instanceof BooleanValue) valueRenderers.add(new BooleanRenderer((BooleanValue) value));
            if (value instanceof ModeValue) valueRenderers.add(new ModeRenderer((ModeValue) value));
            if (value instanceof NumberValue) valueRenderers.add(new NumberRenderer((NumberValue) value));
            if (value instanceof RangeSelectionNumberValue)
                valueRenderers.add(new RangeSelectionNumberRenderer((RangeSelectionNumberValue) value));
            if (value instanceof MultiBooleanValue)
                valueRenderers.add(new MultiBooleanRenderer((MultiBooleanValue) value));
            
        }
    }
    
    public void initGui() {
        valueRenderers.forEach(ValueRenderer::initGui);
    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        animation.run(out ? 1 : 0);
        
        RenderUtils.drawRoundedRect(x, y, width, height, ClickGui.CornerRadius, Colors.OnBackGround.color);
        RenderUtils.drawRoundedRect(x, y, ClickGui.CornerRadius, height, 0, Colors.OnBackGround.color);
        
        float maxHeight = 0;
        for (ValueRenderer<?> valueRenderer : valueRenderers) {
            if (shouldSkipRenderer(valueRenderer)) continue;
            maxHeight += valueRenderer.height + 2;
        }
        
        scrollable.setMax(Math.max(0, maxHeight - 20));
        scrollable.updateScroll();
        
        float yOffset = scrollable.getValuef();
        for (ValueRenderer<?> valueRenderer : valueRenderers) {
            if (shouldSkipRenderer(valueRenderer)) continue;
            
            valueRenderer.width = width;
            valueRenderer.setPosition(x, y + 30 + yOffset);
            valueRenderer.drawScreen(mouseX, mouseY, partialTicks);
            yOffset += valueRenderer.height + 2;
        }
        
        
        float HeaderHeight = 26;
        RenderUtils.drawRoundedRect(x, y - 1, width, HeaderHeight, ClickGui.CornerRadius, Colors.OnBackGround.color);
        RenderUtils.drawRoundedRect(x, y + HeaderHeight - 0.5F, width, 0.5F, 0, Colors.BackGround.color
                .darker().darker().darker().darker().darker());
        FontManager.NotoSans.of(16).drawString("· " + module.getName(),
                x + 5,
                y + (HeaderHeight / 2F) - FontManager.NotoSans.of(16).getHeight() / 3F,
                Colors.Text.color);
    }
    
    private boolean shouldSkipRenderer(ValueRenderer<?> valueRenderer) {
        return valueRenderer.value.getVisible() != null && !valueRenderer.value.getVisible().get();
    }
    
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        valueRenderers.forEach(valueRenderer -> valueRenderer.mouseClicked(mouseX, mouseY, mouseButton));
    }
    
    public void mouseReleased(int mouseX, int mouseY, int state) {
        valueRenderers.forEach(valueRenderer -> valueRenderer.mouseReleased(mouseX, mouseY, state));
    }
}
