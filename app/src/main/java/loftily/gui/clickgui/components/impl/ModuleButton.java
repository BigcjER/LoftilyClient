package loftily.gui.clickgui.components.impl;

import loftily.gui.animation.Animation;
import loftily.gui.animation.Easing;
import loftily.gui.animation.Ripple;
import loftily.gui.clickgui.ClickGui;
import loftily.gui.clickgui.components.Component;
import loftily.gui.clickgui.value.ValuePanel;
import loftily.gui.components.MaterialIcons;
import loftily.gui.font.FontManager;
import loftily.module.Module;
import loftily.utils.render.ColorUtils;
import loftily.utils.render.Colors;
import loftily.utils.render.RenderUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class ModuleButton extends Component {
    public final Module module;
    public final ValuePanel valuePanel;
    private final Animation animation;
    private final Ripple clickRippleAnimation;
    private final ClickGui CGui;
    public boolean binding, hovering;
    
    public ModuleButton(float width, float height, Module module, ClickGui clickGui) {
        super(width, height);
        this.module = module;
        this.animation = new Animation(Easing.EaseOutQuint, 300);
        this.valuePanel = new ValuePanel(module);
        this.clickRippleAnimation = new Ripple();
        
        this.CGui = clickGui;
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        hovering = RenderUtils.isHovering(mouseX, mouseY, x, y, width, height);
        RenderUtils.drawRoundedRect(x, y, width, height, ClickGui.CornerRadius, Colors.OnBackGround.color);
        
        //Ripple
        RenderUtils.startGlStencil(() -> RenderUtils.drawRoundedRect(x, y, width, height, ClickGui.CornerRadius, new Color(0, 0, 0)));
        clickRippleAnimation.draw();
        RenderUtils.stopGlStencil();
        
        //binding
        if (binding) {
            FontManager.NotoSans.of(16).drawString("Press a key to bind", x + 7, y + 14, Colors.Text.color);
            FontManager.NotoSans.of(15).drawString("Clear key bindings", x + 7, y + height / 2F + 5, Colors.SecondaryText.color);
            return;
        }
        
        FontManager.NotoSans.of(16).drawString("· " + module.getName(), x + 5, y + 14, Colors.Text.color);//Name
        String keyName = Keyboard.getKeyName(module.getKey());
        if (!keyName.equalsIgnoreCase("NONE"))
            FontManager.NotoSans.of(15).drawString("Key Binding: " + keyName, x + 7, y + height / 2F + 5, Colors.SecondaryText.color);
        
        //toggled indicator
        animation.run(module.isToggled() ? 255 : 0);
        
        if (animation.getValue() <= 0) return;
        
        RenderUtils.drawRoundedRect(
                x + width - 13.2F,
                y + 13.8F,
                7,
                7,
                ClickGui.CornerRadius - 0.8F,
                ColorUtils.colorWithAlpha(Colors.Active.color, animation.getValuei()));
        
        FontManager.MaterialSymbolsSharp.of(15).drawString(
                MaterialIcons.get("check"),
                x + width - 13.4F,
                y + 15.7F,
                ColorUtils.colorWithAlpha(Colors.BackGround.color, animation.getValuei()));
    }
    
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (binding) {
            if (RenderUtils.isHovering(mouseX, mouseY, x + 7, y + height / 2F + 5, FontManager.NotoSans.of(15).getWidth("Clear key bindings"), FontManager.NotoSans.of(15).getHeight())) {
                module.setKey(0);
                binding = false;
            }
            return;
        }
        
        if (hovering) {
            clickRippleAnimation.add(mouseX, mouseY, width + 120, 800, 35);
            switch (mouseButton) {
                case 0:
                    if (module.isCanBeToggled()) module.toggle();
                    break;
                
                case 1:
                    CGui.setValuePanel(valuePanel);
                    break;
                
                case 2:
                    binding = true;
                    break;
            }
        }
    }
    
    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (binding) {
            if (keyCode != Keyboard.KEY_ESCAPE && keyCode != Keyboard.KEY_LSHIFT) {
                module.setKey(keyCode);
                binding = false;
            }
        }
    }
}
