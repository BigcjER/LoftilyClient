package loftily.gui.clickgui.value;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.clickgui.Colors;
import loftily.gui.clickgui.value.impl.BooleanRenderer;
import loftily.gui.clickgui.value.impl.ModeRenderer;
import loftily.gui.clickgui.value.impl.NumberRenderer;
import loftily.gui.font.FontManager;
import loftily.gui.interaction.Scrollable;
import loftily.module.Module;
import loftily.utils.render.RenderUtils;
import loftily.value.Value;
import loftily.value.impl.BooleanValue;
import loftily.value.impl.NumberValue;
import loftily.value.impl.mode.ModeValue;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ValuePanel {
    public final Animation animation;
    private final List<ValueRenderer<?>> valueRenderers;
    private final Module module;
    private final Scrollable scrollable;
    @Setter
    private float x, y, width, height;
    
    public ValuePanel(Module module) {
        this.module = module;
        this.valueRenderers = new ArrayList<>();
        this.animation = new Animation(Easing.EaseOutQuart, 250);
        this.scrollable = new Scrollable(6);
        
        for (Value<?> value : module.getValues()) {
            if (value instanceof BooleanValue) valueRenderers.add(new BooleanRenderer((BooleanValue) value));
            if (value instanceof ModeValue) valueRenderers.add(new ModeRenderer((ModeValue) value));
            if (value instanceof NumberValue) valueRenderers.add(new NumberRenderer((NumberValue) value));
            
        }
    }
    
    public void initGui() {
        valueRenderers.forEach(ValueRenderer::initGui);
    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Runnable backGroundRunnable = () -> {
            RenderUtils.drawRoundedRect(x, y, width, height, ClickGui.CornerRadius, Colors.OnBackGround.color);
            RenderUtils.drawRoundedRect(x, y, ClickGui.CornerRadius, height, 0, Colors.OnBackGround.color);
        };
        backGroundRunnable.run();
        
        float maxHeight = 0;
        for (ValueRenderer<?> valueRenderer : valueRenderers) {
            if (shouldSkipRenderer(valueRenderer)) continue;
            maxHeight += valueRenderer.height + 2;
        }
        scrollable.setMax(maxHeight);
        scrollable.updateScroll();
        
        RenderUtils.startGlStencil(backGroundRunnable);
        
        FontManager.NotoSans.of(16).drawString("· " + module.getName(), x + 5, y + 14, Colors.Text.color);
        
        float yOffset = scrollable.getValuef();
        for (ValueRenderer<?> valueRenderer : valueRenderers) {
            if (shouldSkipRenderer(valueRenderer)) continue;
            
            valueRenderer.width = width;
            valueRenderer.setPosition(x, y + 30 + yOffset);
            valueRenderer.drawScreen(mouseX, mouseY, partialTicks);
            yOffset += valueRenderer.height + 2;
        }
        
        RenderUtils.stopGlStencil();
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
